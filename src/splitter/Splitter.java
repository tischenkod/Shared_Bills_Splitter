package splitter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Splitter {
    private static final String dateRegexp = "(\\d{4}\\.\\d{2}\\.\\d{2} )?";
    private static final String borrowRegexp = dateRegexp + "borrow [a-zA-Z]+ [a-zA-Z]+ \\d+(\\.\\d*)?";
    private static final String repayRegexp = dateRegexp + "repay [a-zA-Z]+ [a-zA-Z]+ \\d+(\\.\\d*)?";
    private static final String balanceRegexp = dateRegexp + "balance( (open|close))?";
    private static final String groupNameRegexp = "[A-Z]+";
    private static final String groupCreateRegexp = "group create " + groupNameRegexp + " \\([a-zA-Z]+(, [a-zA-Z]+)*\\)";
    private static final String groupShowRegexp = "group show " + groupNameRegexp;
    private static final String purchaseRegexp = dateRegexp + "purchase [a-zA-Z]+ [a-z]+ [1-9]\\d*(.\\d+)? \\([A-Z]+\\)";

    List<Payment> payments;
    List<Person> persons;
    List<Group> groups;


    public Splitter() {
        payments = new LinkedList<>();
        persons = new LinkedList<>();
        groups = new LinkedList<>();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            switch (command) {
                case "help":
                    help();
                    break;
                case "exit":
                    return;
                default:
                    Result result = process(command);
                    if (result != Result.OK) {
                        System.out.println(result);
                    }
            }
        }
    }

    private Result process(String command) {
        if (command.contains("borrow")) {
            return borrow(command);
        }
        if (command.contains("repay")) {
            return repay(command);
        }
        if (command.contains("balance")) {
            return balance(command);
        }
        if (command.contains("group create")) {
            return groupCreate(command);
        }
        if (command.contains("group show")) {
            return groupShow(command);
        }
        if (command.contains("purchase")) {
            return purchase(command);
        }
        return Result.UNKNOWN_COMMAND;
    }

    private Result groupShow(String command) {
        if (!command.matches(groupShowRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        Group toShow = findGroup(command.split(" ")[2]);
        if (toShow == null) {
            System.out.println("Unknown group");
        } else {
            toShow.stream().sorted().forEachOrdered(System.out::println);
        }
        return Result.OK;
    }

    private Result purchase(String command) {
        if (!command.matches(purchaseRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");

        LocalDate date;
        int payerPos;

        if (parts[1].equals("purchase")) {
            payerPos = 2;
            try {
                date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } catch (Exception e) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } else {
            payerPos = 1;
            date = LocalDate.now();
        }
        Person payer = getPerson(parts[payerPos]);

        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[payerPos + 2]).setScale(2, RoundingMode.DOWN);
            if (amount.equals(BigDecimal.ZERO)) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } catch (Exception e) {
            return Result.ILLEGAL_ARGUMENT;
        }

        Group targetGroup = findGroup(parts[payerPos + 3].replaceAll("[()]", ""));
        if (targetGroup == null) {
            System.out.println("Unknown group");
            return Result.OK;
        }

        BigDecimal divisor = new BigDecimal(targetGroup.size());
        BigDecimal sumPerPerson = amount.divide(divisor, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(sumPerPerson.multiply(divisor));

        final BigDecimal oneCent = new BigDecimal("0.01");

        for (Person member: targetGroup) {
            if (!payer.equals(member)) {
                if (remainder.compareTo(BigDecimal.ZERO) == 0){
                    payments.add(new Payment(date, payer, member, sumPerPerson));
                } else {
                    remainder = remainder.subtract(oneCent);
                    payments.add(new Payment(date, payer, member, sumPerPerson.add(oneCent)));
                }

            }
        }

        return Result.OK;
    }

    private Group findGroup(String name) {
        for (Group group: groups) {
            if (group.name.equals(name)) {
                return group;
            }
        }
        return null;
    }

    private Result groupCreate(String command) {
        if (!command.matches(groupCreateRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");
        Group group = new Group(parts[2]);
        groups.add(group);
        for (int i = 3; i < parts.length; i++) {
            int beginShift = i == 3 ? 1 : 0;
            int endShift = 1;//i == parts.length - 1 ? 1 : 0;
            group.add(getPerson(parts[i].substring(beginShift, parts[i].length() - endShift)));
        }
        return Result.OK;
    }

    private Result balance(String command) {
        if (!command.matches(balanceRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");

        int openCloseIndex;
        LocalDate date;

        if (parts[1].equals("balance")) {
            openCloseIndex = 2;
            try {
                date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } catch (Exception e) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } else {
            date = LocalDate.now();
            openCloseIndex = 1;
        }

        if (parts.length == openCloseIndex || parts[openCloseIndex].equals("open")){
            date = date.minusMonths(1);
            date = date.withDayOfMonth(date.lengthOfMonth());
        }

        LocalDate finalDate = date;
        Map<PersonPair, BigDecimal> balanceMap = new HashMap<>();

        payments.stream()
                .filter(p -> !p.date.isAfter(finalDate))
                .forEach(payment -> balanceMap.compute(new PersonPair(payment.pair),
                        (k, v) -> v == null ? payment.amount : payment.amount.add(v)));

        for (Map.Entry<PersonPair, BigDecimal> entry: balanceMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                entry.getKey().swap();
                entry.setValue(entry.getValue().negate());
            }
        }

        if (balanceMap.entrySet()
                .stream().allMatch(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0)) {
            System.out.println("No repayments need");
        } else {
            balanceMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) != 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(es -> System.out.println(es.getValue().compareTo(BigDecimal.ZERO) > 0 ?
                            es.getKey().receiver.toString() + " owes " + es.getKey().sender + " " + es.getValue().setScale(2, RoundingMode.CEILING) :
                            es.getKey().sender + " owes " + es.getKey().receiver + " " + es.getValue().negate().setScale(2, RoundingMode.CEILING)));
        }
        return Result.OK;
    }

    private Result repay(String command) {
        return createPayment(command, Direction.REPLAY);
    }

    private Result createPayment(String command, Direction direction) {
        if (!command.matches(borrowRegexp) && !command.matches(repayRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }

        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");

        int personIndex;
        LocalDate date;

        if (parts[1].equals(direction.delimiter)) {
            personIndex = 2;
            try {
                date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } catch (Exception e) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } else {
            date = LocalDate.now();
            personIndex = 1;
        }

        if (parts[personIndex].equals(parts[personIndex + 1])) {
            return Result.ILLEGAL_ARGUMENT;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[personIndex + 2]).setScale(2, RoundingMode.DOWN);
            if (amount.equals(BigDecimal.ZERO)) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } catch (Exception e) {
            return Result.ILLEGAL_ARGUMENT;
        }

        Person person1 = getPerson(parts[personIndex]);
        Person person2 = getPerson(parts[personIndex + 1]);

        if (direction == Direction.BORROW) {
            payments.add(new Payment(date, person2, person1, amount));
        } else {
            payments.add(new Payment(date, person1, person2, amount));
        }

        return Result.OK;
    }

    private Result borrow(String command) {
        return createPayment(command, Direction.BORROW);
    }

    private Person findPerson(String name) {
        Optional<Person> result = persons.stream()
                .filter(person -> person.name.equals(name))
                .findAny();
        return result.orElse(null);
    }

    private Person getPerson(String name) {
        Person result = findPerson(name);
        if (result == null) {
            Person person = new Person(name);
            persons.add(person);
            return person;
        }
        return result;
    }

    private void help() {
        System.out.println("balance\n" +
                "borrow\n" +
                "exit\n" +
                "group,\n" +
                "help\n" +
                "purchase\n" +
                "repay");
    }
}