import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {
    private ATM atm;
    private BankInterface mockBank;
    private User spyUser;

    @BeforeEach
    void setUp() {
        mockBank = Mockito.mock(BankInterface.class);

        // real User object
        User user = new User("123", "0000", 1000.0);

        spyUser = Mockito.spy(user);

        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm = new ATM(mockBank);
    }

    @Test
    @DisplayName("Insert Card - Successful")
    void testInsertCardSuccessful() {
        when(mockBank.isCardLocked("123")).thenReturn(false);
        when(mockBank.getUserById("123")).thenReturn(spyUser);

        boolean result = atm.insertCard("123");

        assertTrue(result, "Card should be inserted successfully.");
        verify(mockBank, times(1)).getUserById("123");
    }

    @Test
    @DisplayName("Insert Card - Card Locked")
    void testInsertCardCardLocked() {
        when(mockBank.isCardLocked("123")).thenReturn(true);

        boolean result = atm.insertCard("123");

        assertFalse(result, "Card should be locked, insertion should fail.");
    }

    @Test
    @DisplayName("Insert Card - User Not Found")
    void testInsertCardUserNotFound() {
        when(mockBank.isCardLocked("123")).thenReturn(false);
        when(mockBank.getUserById("123")).thenReturn(null);

        boolean result = atm.insertCard("123");

        assertFalse(result, "Card should fail insertion when user is not found.");
    }

    @Test
    @DisplayName("Insert Card - Invalid User ID")
    void testInsertCardInvalidUserId() {
        when(mockBank.isCardLocked("999")).thenReturn(false);
        when(mockBank.getUserById("999")).thenReturn(null);  // No user with ID "999"

        boolean result = atm.insertCard("999");

        assertFalse(result, "Card insertion should fail for invalid user ID.");
    }

    @Test
    @DisplayName("Enter PIN - Correct PIN")
    void testEnterCorrectPin() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");

        boolean result = atm.enterPin("0000");

        assertAll("Test correct PIN login",
                () -> assertTrue(result, "Correct PIN should allow login."),
                () -> {
                    verify(spyUser, times(1)).resetFailedAttempts();
                    assertEquals(0, spyUser.getFailedAttempts(), "Failed attempts should reset to 0.");
                }
        );
    }

    @Test
    @DisplayName("Enter PIN - Incorrect PIN")
    void testEnterIncorrectPin() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");

        boolean result = atm.enterPin("1111");

        assertAll("Test incorrect PIN login",
                () -> assertFalse(result, "Incorrect PIN should not allow login."),
                () -> {
                    verify(spyUser, times(1)).incrementFailedAttempts();
                    assertEquals(1, spyUser.getFailedAttempts(), "Failed attempts should increment by 1.");
                }
        );
    }

    @Test
    @DisplayName("Enter PIN - Lock after 3 Failed Attempts")
    void testLockCardAfterThreeFailedAttempts() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");

        atm.enterPin("1111"); // First attempt
        atm.enterPin("1111"); // Second attempt
        atm.enterPin("1111"); // Third attempt, should lock

        verify(spyUser, times(1)).lockCard();
        assertTrue(spyUser.isLocked(), "User card should be locked after 3 incorrect attempts.");
    }

    @Test
    @DisplayName("Check Balance - Successful")
    void testCheckBalance() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        double balance = atm.checkBalance();

        assertEquals(1000.0, balance, "Balance should match the user's current balance.");
    }

    @Test
    @DisplayName("Check Balance - No Card Inserted")
    void testCheckBalanceNoCard() {
        double balance = atm.checkBalance();

        assertEquals(0, balance, "Balance should be 0 if no card is inserted.");
    }

    @Test
    @DisplayName("Deposit Money")
    void testDepositMoney() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        atm.deposit(500.0);
        assertEquals(1500.0, spyUser.getBalance(), "Balance should increase by deposited amount.");
        verify(spyUser, times(1)).deposit(500.0);
    }

    @Test
    @DisplayName("Deposit Money - No Card Inserted")
    void testDepositMoneyNoCard() {
        atm.deposit(500.0);
        verify(mockBank, never()).getUserById("123");
    }

    @Test
    @DisplayName("Withdraw Money - Sufficient Balance")
    void testWithdrawSufficientBalance() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        boolean result = atm.withdraw(500.0);

        assertTrue(result, "Withdrawal should succeed with sufficient balance.");
        assertEquals(500.0, spyUser.getBalance(), "Balance should decrease by withdrawn amount.");
        verify(spyUser, times(1)).withdraw(500.0);
    }

    @Test
    @DisplayName("Withdraw Money - Insufficient Balance")
    void testWithdrawInsufficientBalance() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        boolean result = atm.withdraw(1500.0);

        assertAll("Test insufficient balance for withdrawal",
                () -> assertFalse(result, "Withdrawal should fail with insufficient balance."),
                () -> assertEquals(1000.0, spyUser.getBalance(), "Balance should remain unchanged after failed withdrawal.")
        );
    }

    @Test
    @DisplayName("Deposit Money - Negative Amount")
    void testDepositNegativeAmount() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        atm.deposit(-100.0);

        assertEquals(1000.0, spyUser.getBalance(), "Balance should remain unchanged after trying to deposit a negative amount.");
    }

    @Test
    @DisplayName("Withdraw Money - No Card Inserted")
    void testWithdrawMoneyNoCard() {
        boolean result = atm.withdraw(500.0);

        assertFalse(result, "Withdrawal should fail if no card is inserted.");
    }

    @Test
    @DisplayName("Show Deposit History with No Deposits")
    void testShowDepositHistoryNoDeposits() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        atm.showDepositHistory();

        List<String> depositHistory = spyUser.getDepositHistory();
        assertEquals(0, depositHistory.size(), "Deposit history should be empty with no deposits.");
    }

    @Test
    @DisplayName("Show Deposit History with deposits")
    void testShowDepositHistory() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        atm.deposit(500.0);
        atm.deposit(200.0);

        atm.showDepositHistory();

        List<String> depositHistory = spyUser.getDepositHistory();

        assertAll("Test deposit history",
                () -> assertEquals(2, depositHistory.size(), "Deposit history should have 2 entries."),
                () -> assertTrue(depositHistory.get(0).contains("Deposited: 500.0"), "First deposit should be 500.0."),
                () -> assertTrue(depositHistory.get(1).contains("Deposited: 200.0"), "Second deposit should be 200.0.")
        );
    }

    @Test
    @DisplayName("Verify Bank Name - Mock Static Method")
    void testBankName() {
        try (MockedStatic<Bank> mockedBank = Mockito.mockStatic(Bank.class)) {
            mockedBank.when(Bank::getBankName).thenReturn("MockBank");

            String bankName = Bank.getBankName();
            System.out.println(bankName);

            assertEquals("MockBank", bankName, "Bank name should be MockBank.");
        }
    }

    @Test
    @DisplayName("End Session - User Logged In")
    void testEndSessionUserLoggedIn() {
        when(mockBank.getUserById("123")).thenReturn(spyUser);
        atm.insertCard("123");
        atm.enterPin("0000");

        atm.endSession();

        double balanceAfterSessionEnd = atm.checkBalance();
        assertEquals(0, balanceAfterSessionEnd, "Balance should return 0 if session is ended and no user is active.");
    }

    @Test
    @DisplayName("End Session - No User Logged In")
    void testEndSessionNoUserLoggedIn() {
        atm.endSession();

        double balance = atm.checkBalance();
        assertEquals(0, balance, "Balance should be 0 if no session was active.");
    }
}
