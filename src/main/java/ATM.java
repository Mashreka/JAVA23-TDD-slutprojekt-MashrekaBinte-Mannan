import java.util.List;

public class ATM {
    private BankInterface bank;
    private User currentUser;

    public ATM(BankInterface bank) {
        this.bank = bank;
    }

    public boolean insertCard(String userId) {
        if (bank.isCardLocked(userId)) {
            System.out.println("Card is locked.");
            return false;
        }
        currentUser = bank.getUserById(userId);
        if (currentUser == null) {
            System.out.println("User not found.");
            return false;
        }
        System.out.println("Insert successful.");
        return true;
    }

    public boolean enterPin(String pin) {
        if (currentUser == null) {
            System.out.println("No card inserted.");
            return false;
        }

        if (currentUser.isLocked()) {
            System.out.println("Card is locked.");
            return false;
        }

        if (currentUser.getPin().equals(pin)) {
            currentUser.resetFailedAttempts();
            System.out.println("Login successful.");
            return true;
        } else {
            currentUser.incrementFailedAttempts();
            if (currentUser.getFailedAttempts() >= 3) {
                currentUser.lockCard();
                System.out.println("Card locked due to 3 incorrect attempts.");
            } else {
                System.out.println("Incorrect PIN. Attempts remaining: " + (3 - currentUser.getFailedAttempts()));
            }
            return false;
        }
    }

    public double checkBalance() {
        if (currentUser != null) {
            System.out.println(currentUser.getBalance());
            return currentUser.getBalance();
        }
        System.out.println("No card inserted.");
        return 0;
    }

    public void deposit(double amount) {
        if (currentUser != null) {
            if (amount > 0) {
                currentUser.deposit(amount);
                System.out.println("Deposit successful. New balance: " + currentUser.getBalance());
            } else {
                System.out.println("Invalid deposit amount. Deposit failed.");
            }
        } else {
            System.out.println("No card inserted.");
        }
    }

    public boolean withdraw(double amount) {
        if (currentUser != null) {
            if (currentUser.getBalance() >= amount) {
                currentUser.withdraw(amount);
                System.out.println("Withdrawal successful. New balance: " + currentUser.getBalance());
                return true;
            } else {
                System.out.println("Insufficient funds.");
            }
        } else {
            System.out.println("No card inserted.");
        }
        return false;
    }

    public void showDepositHistory() {
        if (currentUser != null) {
            System.out.println("Deposit History:");
            List<String> history = currentUser.getDepositHistory();
            if (history.isEmpty()) {
                System.out.println("No deposits made.");
            } else {
                for (String depositHistory : history) {
                    System.out.println(depositHistory);
                }
            }
        } else {
            System.out.println("No card inserted.");
        }
    }

    public void endSession() {
        if (currentUser != null) {
            System.out.println("Session ended.");
            currentUser = null; // Clear the current user to indicate no active session
        } else {
            System.out.println("No active session to end.");
        }
    }

}
