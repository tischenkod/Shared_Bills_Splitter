import org.hyperskill.hstest.dynamic.output.InfiniteLoopDetector;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.SimpleTestCase;
import org.hyperskill.hstest.testcase.TestCase;
import org.hyperskill.hstest.testing.TestedProgram;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class SharedBillsSplitterTestStage3 extends StageTest {

    static {
        InfiniteLoopDetector.setWorking(false);
    }

    private static final String UNKNOWN_COMMAND = "Unknown command";
    private static final String EXIT_ERROR = "Your program should stop after exit command";
    private static final String HELP_ERROR = "Help command should print all commands line by line in natural order";
    private static final String ILLEGAL_COMMAND_ARGUMENTS = "Illegal command arguments";
    private static final String ILLEGAL_ARGUMENTS_ERROR = "Your program should handle exceptions in incorrect command arguments input";
    private static final String UNKNOWN_GROUP = "Unknown group";
    private static final String NO_REPAYMENTS_NEED = "No repayments need";
    private static final String WRONG_CALCULATIONS = "Wrong calculations. Program should output owes list that " +
            "if every person in this list repay his owes then everyone will have zero balance and everyone will be paid off";

    enum Commands {
        help,
        borrow,
        repay,
        balance,
        exit,
        group,
        purchase
    }

    @Override
    public List<TestCase> generate() {
        return List.of(
                new TestCase<String>()
                        .setCheckFunc(this::checkUnknownCommand)
                        .setAttach("someAttachText")
                        .setInput("someRandomText\n" +
                                "exit"),

                new SimpleTestCase("" +
                        "repay Ann\n" +
                        "exit",
                        ILLEGAL_COMMAND_ARGUMENTS)
                        .setFeedback(ILLEGAL_ARGUMENTS_ERROR),

                new TestCase<String>()
                        .setCheckFunc(this::checkHelpCommand)
                        .setInput(concatLines(Commands.help.toString(), Commands.exit.toString())),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute(Commands.exit.toString());
                    if (!main.isFinished()) {
                        return CheckResult.wrong(EXIT_ERROR);
                    }
                    return CheckResult.correct();
                }),

                new SimpleTestCase(
                        concatLines("2020.09.30 borrow Ann Bob 20.10",
                                "2020.10.01 repay Ann Bob 10.22",
                                "2020.10.10 borrow Bob Ann 7.35",
                                "2020.10.15 repay Ann Bob 8.99",
                                "repay Bob Ann 6.46",
                                "2020.09.25 balance",
                                "2020.10.30 balance open",
                                "2020.10.20 balance close",
                                "balance close",
                                "exit"),
                        concatLines(
                                NO_REPAYMENTS_NEED,
                                "Ann owes Bob 20.10",
                                "Bob owes Ann 6.46",
                                NO_REPAYMENTS_NEED)
                ).setFeedback("Money should be formatted with properly scale"),

                new SimpleTestCase(
                        concatLines("borrow Ann Bob 25",
                                "repay Ann Bob 15",
                                "repay Bob Chuck 7",
                                "borrow Ann Bob 4",
                                "repay Bob Diana 5",
                                "borrow Elon Diana 12",
                                "repay Chuck Diana 14",
                                "repay Chuck Diana 12",
                                "balance close",
                                "exit"),
                        concatLines(
                                "Ann owes Bob 14.00",
                                "Chuck owes Bob 7.00",
                                "Diana owes Bob 5.00",
                                "Diana owes Chuck 26.00",
                                "Elon owes Diana 12.00")
                ).setFeedback("Owes should be sorted by Person who owes and Person whom owes"),

                new TestCase().setDynamicTesting(() -> {
                    Random random = new Random();
                    List<String> persons = List.of("Annabelle", "Billibob", "Carlos", "Diana", "Elon", "Finny");
                    String keyPerson = persons.get(random.nextInt(persons.size()));
                    BigDecimal keyBalanceBorrow = BigDecimal.ZERO;
                    BigDecimal keyBalanceRepay = BigDecimal.ZERO;
                    TestedProgram main = new TestedProgram();
                    main.start();
                    for (int i = 0; i < 100; i++) {
                        String personFrom = persons.get(random.nextInt(persons.size()));
                        String personTo = persons.get(random.nextInt(persons.size()));
                        if (personFrom.equalsIgnoreCase(personTo)) {
                            continue;
                        }
                        Commands command;
                        BigDecimal amount = new BigDecimal(String.format("%d.%d", random.nextInt(200), random.nextInt(99)));
                        if (random.nextBoolean()) {
                            command = Commands.borrow;
                            if (personFrom.equals(keyPerson)) {
                                keyBalanceBorrow = keyBalanceBorrow.add(amount);
                            }
                            if (personTo.equals(keyPerson)) {
                                keyBalanceBorrow = keyBalanceBorrow.subtract(amount);
                            }
                        } else {
                            command = Commands.repay;
                            if (personFrom.equals(keyPerson)) {
                                keyBalanceRepay = keyBalanceRepay.add(amount);
                            }
                            if (personTo.equals(keyPerson)) {
                                keyBalanceRepay = keyBalanceRepay.subtract(amount);
                            }
                        }
                        String line = String.format("%s %s %s %s", command, personFrom, personTo, amount);
                        main.execute(line);
                    }
                    String result = main.execute("balance close");
                    Optional<BigDecimal> sum = Arrays.stream(result.split("\n"))
                            .filter(it -> it.contains(keyPerson))
                            .map(it -> {
                                String[] split = it.split("\\s+");
                                Character sign = it.startsWith(keyPerson) ? '+' : '-';
                                return sign + split[split.length - 1];
                            })
                            .map(BigDecimal::new)
                            .reduce(BigDecimal::add);

                    BigDecimal sumBalance = keyBalanceBorrow.subtract(keyBalanceRepay);
                    if (sumBalance.compareTo(sum.orElse(BigDecimal.ZERO)) == 0) {
                        return CheckResult.correct();
                    }
                    return CheckResult.wrong(WRONG_CALCULATIONS);
                }),


                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    if (!main.execute("group create lowerCaseText").contains(ILLEGAL_COMMAND_ARGUMENTS)) {
                        return CheckResult.wrong(String.format("Group name must be UPPERCASE, otherwise \"%s\" should be printed",
                                ILLEGAL_COMMAND_ARGUMENTS));
                    }
                    if (!main.execute("group show NOTFOUNDGROUP").contains(UNKNOWN_GROUP)) {
                        return CheckResult.wrong("It should be printed \"%s\" if the group have not been created yet");
                    }

                    main.execute("group create BOYS (Elon, Bob, Chuck)");
                    String showGroupResult = main.execute("group show BOYS").trim();
                    if (!equalsByLines(showGroupResult, "" +
                            "Bob\n" +
                            "Chuck\n" +
                            "Elon")) {
                        return CheckResult.wrong("Persons should be printed line by line sorted in ascending order");
                    }
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create COFFEETEAM (Ann, Bob)");
                    main.execute("purchase Bob coffee 10 (COFFEETEAM)");
                    String balanceFirst = main.execute("balance close").trim();
                    if (!balanceFirst.equals("Ann owes Bob 5.00")) {
                        return CheckResult.wrong("Only Ann owes Bob. Bob should not owe to himself");
                    }
                    main.execute("repay Ann Bob 5.00");
                    String balanceSecond = main.execute("balance close").trim();
                    if (!balanceSecond.equals(NO_REPAYMENTS_NEED)) {
                        return CheckResult.wrong("If everybody owes zero, it should be printed \"No repayments need\"");
                    }
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create FRIENDS (Ann, Bob, Chuck)");
                    main.execute("purchase Elon chocolate 12.50 (FRIENDS)");
                    String balanceResult = main.execute("balance close");
                    if (!equalsByLines(balanceResult, "" +
                            "Ann owes Elon 4.17\n" +
                            "Bob owes Elon 4.17\n" +
                            "Chuck owes Elon 4.16")) {
                        return CheckResult.wrong("Output should be the same as in example");
                    }
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create BOYS (Elon, Bob, Chuck)");
                    main.execute("group create GIRLS (Ann, Diana)");
                    main.execute("2020.10.20 purchase Diana flowers 15.65 (BOYS)");
                    main.execute("2020.10.21 purchase Chuck chocolate 6.30 (BOYS)");
                    main.execute("2020.10.22 purchase Bob icecream 3.99 (GIRLS)");
                    String balanceCloseResult = main.execute("balance close");
                    if (!equalsByLines(balanceCloseResult, "" +
                            "Ann owes Bob 2.00\n" +
                            "Bob owes Chuck 2.10\n" +
                            "Bob owes Diana 3.23\n" +
                            "Chuck owes Diana 5.22\n" +
                            "Elon owes Chuck 2.10\n" +
                            "Elon owes Diana 5.21"))
                        return CheckResult.wrong("Output should be the same as in example");
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create GIRLS (Ann, Diana)");
                    main.execute("group create TEAM (+Bob, GIRLS, -Frank, Chuck)");
                    String groupResult = main.execute("group show TEAM");
                    if (!equalsByLines(groupResult, "" +
                            "Ann\n" +
                            "Bob\n" +
                            "Chuck\n" +
                            "Diana")) {
                        return CheckResult.wrong("Program should include Bob, Chuck and persons from GIRLS, also Frank should be excluded");
                    }
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create GIRLS (Ann, Diana)");
                    main.execute("group create TEAM (+Bob, GIRLS, -Frank, Chuck)");
                    main.execute("2020.10.20 purchase Diana flowers 15.65 (TEAM, Elon, -GIRLS)");
                    main.execute("2020.10.21 purchase Elon ChuckBirthdayGift 20.99 (TEAM, -Chuck)");
                    String balanceResult = main.execute("balance close");
                    if (!equalsByLines(balanceResult, "" +
                            "Ann owes Elon 7.00\n" +
                            "Bob owes Diana 5.22\n" +
                            "Bob owes Elon 7.00\n" +
                            "Chuck owes Diana 5.22\n" +
                            "Diana owes Elon 1.78")) {
                        return CheckResult.wrong("Program should split flowers bill on TEAM with Elon without GIRLS");
                    }
                    return CheckResult.correct();
                }),

                new TestCase().setDynamicTesting(() -> {
                    TestedProgram main = new TestedProgram();
                    main.start();
                    main.execute("group create SOMEGROUP (Bob)");
                    main.execute("group create GIRLS (Ann, Diana)");
                    main.execute("group create BOYS (Bob, Chuck, Elon)");
                    main.execute("group add SOMEGROUP (GIRLS, Frank)");
                    main.execute("group remove SOMEGROUP (-BOYS, Bob, +Frank)");
                    String groupResult = main.execute("group show SOMEGROUP");
                    if (!equalsByLines(groupResult, "Ann\n" +
                            "Bob\n" +
                            "Diana")) {
                        return CheckResult.wrong("First of all program should collect persons from brackets:" +
                                "At first collect all additions, and then remove all persons to delete." +
                                "eg. group <some group command> GROUP (-BOYS, Bob, +Frank): " +
                                "at first program should collect Bob and Frank" +
                                "and then remove all persons from BOYS");
                    }
                    return CheckResult.correct();
                })

        );
    }

    private CheckResult checkHelpCommand(String reply, String attach) {
        String[] replyArr = reply.split("\n");
        List<String> commandList = getCommandList();
        if (replyArr.length != commandList.size()) {
            return CheckResult.wrong(HELP_ERROR);
        }
        for (int i = 0; i < replyArr.length; i++) {
            if (!replyArr[i].toLowerCase().startsWith(commandList.get(i).toLowerCase())) {
                return CheckResult.wrong(HELP_ERROR);
            }
        }
        return CheckResult.correct();
    }

    private CheckResult checkUnknownCommand(String reply, String attach) {
        try {
            reply = reply.trim();
            Commands command = Commands.valueOf(reply);;
        } catch (IllegalArgumentException e) {
            if (!reply.toLowerCase().startsWith(UNKNOWN_COMMAND.toLowerCase())) {
                return CheckResult.wrong(String.format("For unknown command output should starts with: %s", UNKNOWN_COMMAND));
            }
        }
        return CheckResult.correct();
    }

    private List<String> getCommandList() {
        return Arrays.stream(Commands.values())
                .map(Enum::toString)
                .sorted().collect(Collectors.toList());
    }

    private String concatLines(List<String> strings) {
        return String.join("\n", strings);
    }

    private String concatLines(String... strings) {
        return String.join("\n", strings);
    }

    private static boolean equalsByLines(String sample, String linesStr) {
        List<String> sampleLines = strToLinesTrimmed(sample);
        List<String> lines = strToLinesTrimmed(linesStr);
        return sampleLines.equals(lines);
    }

    private static List<String> strToLinesTrimmed(String sample) {
        return sample.lines().map(String::trim).collect(Collectors.toList());
    }
}