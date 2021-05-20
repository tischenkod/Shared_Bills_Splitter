package splitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import splitter.entities.Group;
import splitter.entities.Payment;
import splitter.entities.Person;
import splitter.entities.PersonPair;
import splitter.services.GroupService;
import splitter.services.PaymentService;
import splitter.services.PersonService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Splitter {
    private static final String dateRegexp = "(\\d{4}\\.\\d{2}\\.\\d{2} )?";
    private static final String actionItemRegexp = "[+-]?[a-zA-Z]+";
    private static final String actionItemsRegexp = " \\(" + actionItemRegexp + "(,\\s*" + actionItemRegexp + ")*\\)";
    private static final String paymentRegexp = dateRegexp + "(borrow|repay) [a-zA-Z]+ [a-zA-Z]+ \\d+(\\.\\d*)?";
    private static final String balanceRegexp = dateRegexp + "balance(\\s(open|close))?(" + actionItemsRegexp + ")?";
    private static final String groupNameRegexp = "[A-Z]+";
    private static final String groupModRegexp = "group (create|add|remove) " + groupNameRegexp + actionItemsRegexp;
    private static final String groupShowRegexp = "group show " + groupNameRegexp;
    private static final String groupPaymentRegexp = dateRegexp
            + "(purchase|cashBack) [a-zA-Z]+ [a-zA-Z]+ [1-9]\\d*(.\\d+)?" + actionItemsRegexp;
    private static final String writeOffRegexp = dateRegexp + "writeOff";
    private static final String secretSantaRegexp = "secretSanta " + groupNameRegexp;

    @Autowired
    PaymentService paymentsService;

    @Autowired
    PersonService personsService;

    @Autowired
    GroupService groupsService;

    public Splitter() {
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
        if (command.contains("group show")) {
            return groupShow(command);
        }
        if (command.contains("group")) {
            return groupModify(command);
        }
        if (command.contains("purchase")) {
            return purchase(command);
        }
        if (command.contains("cashBack")) {
            return cashback(command);
        }
        if (command.contains("writeOff")) {
            return writeOff(command);
        }
        if (command.contains("secretSanta")) {
            return secretSanta(command);
        }
        return Result.UNKNOWN_COMMAND;
    }

    private Result secretSanta(String command) {
        if (!command.matches(secretSantaRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        Group toGift = groupsService.findGroup(command.replaceAll(" {2}", " ").trim().split(" ")[1]);
        if (toGift == null) {
            System.out.println("Unknown group");
        } else {
            List<Person> members = new LinkedList<>(toGift.getMembers());
            Collections.shuffle(members);
            Set<PersonPair> giftPairs = new TreeSet<>();
            Person prev = null;
            for (Person item: members) {
                if (prev != null) {
                    giftPairs.add(new PersonPair(prev, item));
                }
                prev = item;
            }
            giftPairs.add(new PersonPair(prev, members.get(0)));
            giftPairs.forEach(item -> System.out.println(item.getSender() + " gift to " + item.getReceiver()));
        }
        return Result.OK;
    }

    private Result writeOff(String command) {
        if (!command.matches(writeOffRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }

        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");

        LocalDate date;

        if (parts[0].equals("writeOff")) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } catch (Exception e) {
                return Result.ILLEGAL_ARGUMENT;
            }
        }

        paymentsService.writeOff(date);

        return Result.OK;
    }

    private Result groupShow(String command) {
        if (!command.matches(groupShowRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        Group toShow = groupsService.findGroup(command.split(" ")[2]);
        if (toShow == null) {
            System.out.println("Unknown group");
        } else {
            toShow.stream().sorted().forEachOrdered(System.out::println);
        }
        return Result.OK;
    }

    private Result groupModify(String command) {
        if (!command.matches(groupModRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(",", ", ").replaceAll(" {2}", " ").trim().split(" ");
        Group targetGroup;
        targetGroup = groupsService.findGroup(parts[2]);

        Set<Person> actionSet = toActionSet(Arrays.copyOfRange(parts, 3, parts.length));
        if (actionSet == null) {
            return Result.OK;
        }

        switch (parts[1]) {
            case "create":
                if (targetGroup != null) {
                    groupsService.remove(targetGroup);
                }
                targetGroup = new Group(parts[2]);
                targetGroup.addAll(actionSet);
                groupsService.save(targetGroup);
                break;
            case "add":
                if (targetGroup == null) {
                    System.out.println("Group " + parts[2] + " not found");
                    return Result.OK;
                }
                targetGroup.addAll(actionSet);
                groupsService.save(targetGroup);
                break;
            case "remove":
                if (targetGroup == null) {
                    System.out.println("Group " + parts[2] + " not found");
                    return Result.OK;
                }
                targetGroup.removeAll(actionSet);
                if (targetGroup.isEmpty()) {
                    groupsService.remove(targetGroup);
                }
                groupsService.save(targetGroup);
        }


        return Result.OK;
    }

    private Set<Person> toActionSet(String[] tokens) {
        Set<Person> resultSet = new TreeSet<>();
        Set<Person> skipSet = new TreeSet<>();

        for (int i = 0; i < tokens.length; i++) {
            int beginShift = i == 0 ? 1 : 0;
            String name = tokens[i].substring(beginShift, tokens[i].length() - 1);
            char prefix;
            if (name.charAt(0) == '-') {
                prefix = '-';
                name = name.substring(1);
            } else if (name.charAt(0) == '+') {
                prefix = '+';
                name = name.substring(1);
            } else {
                prefix = '+';
            }
            if (name.matches(groupNameRegexp)) {
                Group groupItem = groupsService.findGroup(name);
                if (groupItem == null) {
                    System.out.println("Group " + name + " not found");
                    return null;
                }
                (prefix == '+' ? resultSet : skipSet).addAll(groupItem.getMembers());
            } else {
                (prefix == '+' ? resultSet : skipSet).add(personsService.getPerson(name));
            }
        }
        resultSet.removeAll(skipSet);
        return resultSet;
    }

    private Result balance(String command) {
        if (!command.matches(balanceRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(",", ", ")
                .replaceAll(" {2}", " ")
                .trim()
                .split(" ");

        int openCloseIndex;
        LocalDate date;

        if (parts.length > 1 && parts[1].equals("balance")) {
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

        int filterIndex;

        BalanceType balanceType;

        if (parts.length == openCloseIndex) {
            balanceType = BalanceType.OPEN;
            filterIndex = openCloseIndex;
        } else {
            switch (parts[openCloseIndex]) {
                case "open":
                    balanceType = BalanceType.OPEN;
                    filterIndex = openCloseIndex + 1;
                    break;
                case "close":
                    balanceType = BalanceType.CLOSE;
                    filterIndex = openCloseIndex + 1;
                    break;
                default:
                    balanceType = BalanceType.OPEN;
                    filterIndex = openCloseIndex;
            }
        }

        if (balanceType == BalanceType.OPEN) {
            date = date.minusMonths(1);
            date = date.withDayOfMonth(date.lengthOfMonth());
        }

        final Set<Person> filter = filterIndex == parts.length ?
                null :
                toActionSet(Arrays.copyOfRange(parts, filterIndex, parts.length));

        Map<PersonPair, BigDecimal> balanceMap = new HashMap<>();

//        Set<Person> zeroOwe =
        if (filter != null) {
            paymentsService.balance(date)
                    .stream()
                    .filter(summary -> filter.contains(summary.sender) || filter.contains(summary.receiver))
                    .collect(Collectors.toMap(PaymentSummary::getSender, PaymentSummary::getAmount, BigDecimal::add))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(BigDecimal.ZERO))
                    .map(Map.Entry::getKey).forEach(filter::remove);
        }

        paymentsService.balance(date)
                .stream()
                .filter(summary -> filter == null || filter.contains(summary.sender) || filter.contains(summary.receiver))
                .forEach(payment -> balanceMap.compute(new PersonPair(payment.getSender(), payment.getReceiver()),
                        (k, v) -> v == null ? payment.getAmount() : payment.getAmount().add(v)));

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
                            es.getKey().getReceiver().toString() + " owes " + es.getKey().getSender() + " " + es.getValue().setScale(2, RoundingMode.CEILING) :
                            es.getKey().getSender() + " owes " + es.getKey().getReceiver() + " " + es.getValue().negate().setScale(2, RoundingMode.CEILING)));
        }
        return Result.OK;
    }

    private Result borrow(String command) {
        return createPayment(command, PaymentDirection.BORROW);
    }

    private Result repay(String command) {
        return createPayment(command, PaymentDirection.REPAY);
    }

    private Result createPayment(String command, PaymentDirection paymentDirection) {
        if (!command.matches(paymentRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }

        String[] parts = command.replaceAll(" {2}", " ").trim().split(" ");

        int personIndex;
        LocalDate date;

        if (parts[1].equals(paymentDirection.delimiter)) {
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

        Person person1 = personsService.getPerson(parts[personIndex]);
        Person person2 = personsService.getPerson(parts[personIndex + 1]);

        if (paymentDirection == PaymentDirection.BORROW) {
            paymentsService.add(new Payment(date, person2, person1, amount));
        } else {
            paymentsService.add(new Payment(date, person1, person2, amount));
        }

        return Result.OK;
    }

    private Result purchase(String command) {
        return createGroupPayment(command, GroupPaymentDirection.PURCHASE);
    }

    private Result cashback(String command) {
        return createGroupPayment(command, GroupPaymentDirection.CASHBACK);
    }

    private Result createGroupPayment(String command, GroupPaymentDirection paymentDirection) {
        if (!command.matches(groupPaymentRegexp)) {
            return Result.ILLEGAL_ARGUMENT;
        }
        String[] parts = command.replaceAll(",", ", ").replaceAll(" {2}", " ").trim().split(" ");

        LocalDate date;
        int payerPos;

        if (parts[1].equals(paymentDirection.delimiter)) {
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
        Person payer = personsService.getPerson(parts[payerPos]);

        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[payerPos + 2]).setScale(2, RoundingMode.DOWN);
            if (amount.equals(BigDecimal.ZERO)) {
                return Result.ILLEGAL_ARGUMENT;
            }
        } catch (Exception e) {
            return Result.ILLEGAL_ARGUMENT;
        }

        final BigDecimal oneCent;

        if (paymentDirection == GroupPaymentDirection.CASHBACK) {
            amount = amount.negate();
            oneCent = new BigDecimal("-0.01");
        } else {
            oneCent = new BigDecimal("0.01");
        }

        Set<Person> targetSet = toActionSet(Arrays.copyOfRange(parts, payerPos + 3, parts.length));
        if (targetSet == null) {
            return Result.OK;
        }

        BigDecimal divisor = new BigDecimal(targetSet.size());
        targetSet.remove(payer);
        BigDecimal sumPerPerson = amount.divide(divisor, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(sumPerPerson.multiply(divisor));

        for (Person member: targetSet) {
            if (remainder.compareTo(BigDecimal.ZERO) == 0){
                paymentsService.add(new Payment(date, payer, member, sumPerPerson));
            } else {
                remainder = remainder.subtract(oneCent);
                paymentsService.add(new Payment(date, payer, member, sumPerPerson.add(oneCent)));
            }

        }

        return Result.OK;
    }



    private void help() {
        System.out.println("balance\n" +
                "borrow\n" +
                "cashBack\n" +
                "exit\n" +
                "group\n" +
                "help\n" +
                "purchase\n" +
                "repay\n" +
                "secretSanta\n" +
                "writeOff");
    }
}
