package splitter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class Splitter {
    final String dateRegexp = "(\\d{4}\\.\\d{2}\\.\\d{2} )?";
    final String borrowRegexp = dateRegexp + "borrow [a-zA-Z]+ [a-zA-Z]+ \\d+";
    final String repayRegexp = dateRegexp + "repay [a-zA-Z]+ [a-zA-Z]+ \\d+";
    final String balanceRegexp = dateRegexp + "balance( (open|close))?";

    List<Payment> payments;
    List<Person> persons;

    public Splitter() {
        payments = new LinkedList<>();
        persons = new LinkedList<>();
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
        return Result.UNKNOWN_COMMAND;
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
        Map<PersonPair, Integer> balanceMap = new HashMap<>();

        payments.stream()
                .filter(p -> !p.date.isAfter(finalDate))
                .forEach(payment -> balanceMap.compute(payment.pair,
                        (k, v) -> v == null ? payment.amount : v + payment.amount));

        if (balanceMap.entrySet()
                .stream().noneMatch(entry -> entry.getValue() != 0)) {
            System.out.println("No repayments need");
        } else {
            balanceMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(es -> System.out.println(es.getValue() > 0 ?
                            es.getKey().receiver.toString() + " owes " + es.getKey().sender + " " + es.getValue() :
                            es.getKey().sender + " owes " + es.getKey().receiver + " " + -es.getValue()));
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

        int amount;
        try {
            amount = Integer.parseInt(parts[personIndex + 2]);
            if (amount == 0) {
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

    private Person getPerson(String name) {
        Optional<Person> result = persons.stream()
                .filter(person -> person.name.equals(name))
                .findAny();
        if (result.isEmpty()) {
            Person person = new Person(name);
            persons.add(person);
            return person;
        }
        return result.get();
    }

    private void help() {
        System.out.println("balance\n" +
                "borrow\n" +
                "exit\n" +
                "help\n" +
                "repay");
    }
}
