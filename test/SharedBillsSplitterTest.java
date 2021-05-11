import org.hyperskill.hstest.dynamic.output.InfiniteLoopDetector;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.SimpleTestCase;
import org.hyperskill.hstest.testcase.TestCase;
import org.hyperskill.hstest.testing.TestedProgram;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class SharedBillsSplitterTest extends StageTest {

    static {
        InfiniteLoopDetector.setWorking(false);
    }

    private static final String UNKNOWN_COMMAND = "Unknown command";
    public static final String EXIT_ERROR = "Your program should stop after exit command.";
    public static final String HELP_ERROR = "help command should print all commands line by line in natural order.";
    private static final String ILLEGAL_ARGUMENTS_ERROR = "Your program should handle exceptions in incorrect command arguments input";

    enum Commands {
        help,
        borrow,
        repay,
        balance,
        exit
    }

    @Override
    public List<TestCase> generate() {
        return List.of(
            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                main.start();
                main.execute(Commands.exit.toString());
                if (!main.isFinished()) {
                    return CheckResult.wrong(EXIT_ERROR);
                }
                return CheckResult.correct();
            }),
            new TestCase<String>()
                .setCheckFunc(this::checkUnknownCommand)
                .setAttach("someAttachText")
                .setInput("someRandomText\nexit"),

            new SimpleTestCase("repay Ann\nexit", "Illegal command arguments")
                .setFeedback(ILLEGAL_ARGUMENTS_ERROR),

            new TestCase<String>()
                .setCheckFunc(this::checkHelpCommand)
                .setInput(Commands.help.toString() + "\nexit"),

            new SimpleTestCase(
                concatLines("2020.09.30 borrow Ann Bob 20",
                    "2020.10.01 repay Ann Bob 10",
                    "2020.10.10 borrow Bob Ann 7",
                    "2020.10.15 repay Ann Bob 8",
                    "repay Bob Ann 5",
                    "2020.09.25 balance",
                    "2020.10.30 balance open",
                    "2020.10.20 balance close",
                    "balance close",
                    "exit"),
                concatLines(
                    "No repayments need",
                    "Ann owes Bob 20",
                    "Bob owes Ann 5",
                    "No repayments need")
            ).setFeedback("Expected 4 lines."),

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
                    "Ann owes Bob 14",
                    "Chuck owes Bob 7",
                    "Diana owes Bob 5",
                    "Diana owes Chuck 26",
                    "Elon owes Diana 12"
                )
            ).setFeedback("Expecting 5 lines. Owes should be sorted by Person who Owes and Person whom owes"),

            new TestCase().setDynamicTesting(() -> {
                Random random = new Random();

                List<String> persons = List.of("Annabelle", "Billibob", "Carlos", "Diana", "Elon", "Finny");
                String keyPerson = persons.get(random.nextInt(persons.size()));
                BigInteger keyBalanceBorrow = BigInteger.ZERO;
                BigInteger keyBalanceRepay = BigInteger.ZERO;

                TestedProgram main = new TestedProgram();
                main.start();
                for (int i = 0; i < 100; i++) {
                    String personFrom = persons.get(random.nextInt(persons.size()));
                    String personTo = persons.get(random.nextInt(persons.size()));
                    if (personFrom.equalsIgnoreCase(personTo)) {
                        continue;
                    }
                    String command;
                    BigInteger amount = new BigInteger(String.valueOf(random.nextInt(200)));
                    if (random.nextBoolean()) {
                        command = "borrow";
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.subtract(amount);
                        }
                    } else {
                        command = "repay";
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.subtract(amount);
                        }
                    }
                    String line = String.format("%s %s %s %d", command, personFrom, personTo, amount);
                    main.execute(line);
                }
                String result = main.execute("balance close\nexit");
                Optional<BigInteger> sum = Arrays.stream(result.split("\n"))
                    .filter(it -> it.contains(keyPerson))
                    .map(it -> {
                        String[] split = it.split("\\s+");
                        Character sign = it.startsWith(keyPerson) ? '+' : '-';
                        return sign + split[split.length - 1];
                    })
                    .map(BigInteger::new)
                    .reduce(BigInteger::add);

                BigInteger sumBalance = keyBalanceBorrow.subtract(keyBalanceRepay);
                if (sumBalance.compareTo(sum.orElse(BigInteger.ZERO)) == 0) {
                    return CheckResult.correct();
                }

                return CheckResult.wrong("Wrong calculations");
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
            Commands command = Commands.valueOf(reply);
            ;
        } catch (IllegalArgumentException e) {
            if (!reply.toLowerCase().startsWith(UNKNOWN_COMMAND.toLowerCase())) {
                return CheckResult.wrong(
                    "For unknown command output should start from: " + UNKNOWN_COMMAND);
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
}
