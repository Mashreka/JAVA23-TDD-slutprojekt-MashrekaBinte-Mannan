# ATM Simulation Project (TDD)

This project simulates an ATM system interacting with a mock bank. The goal of this project is to implement the functionality of an ATM while following the principles of **Test-Driven Development (TDD)**.

## Features

- Inserting a card and verifying user details.
- Entering a PIN and managing lock status after three failed attempts.
- Checking balance, depositing money, and withdrawing funds while ensuring sufficient balance.
- Viewing deposit history and safely ending a session.

## Testing Approach

In this project, I use **Mockito** to mock the **BankInterface**. The `spyUser` is a real instance of the `User` class, but with **Mockito’s spy** functionality applied to monitor method calls, such as `deposit()`, `withdraw()`, and `resetFailedAttempts()`. The `spyUser` helps ensure the actual `User` object is used while tracking method interactions and verifying their correct execution.

Key functions like user authentication, balance checks, and static methods for bank identification were tested thoroughly. All tests were written before implementation, ensuring high-quality, maintainable code and achieving over 80% test coverage.

## Conclusion

This project demonstrates the effective use of TDD to ensure that the ATM system behaves as expected while interacting with a mock bank.
