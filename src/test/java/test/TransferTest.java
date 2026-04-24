package test;

import data.DataHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import page.DashboardPage;
import page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest {

    private DashboardPage dashboardPage;

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor();
        dashboardPage = verificationPage.validVerify(verificationCode);
    }

    @Test
    void shouldTransferMoneyBetweenOwnCards() {
        int balanceFirstBefore = dashboardPage.getCardBalance(0);
        int balanceSecondBefore = dashboardPage.getCardBalance(1);
        int amount = 500;

        var transferPage = dashboardPage.selectCardToTransfer(0);
        dashboardPage = transferPage.makeTransfer(String.valueOf(amount), DataHelper.getSecondCardInfo());

        int balanceFirstAfter = dashboardPage.getCardBalance(0);
        int balanceSecondAfter = dashboardPage.getCardBalance(1);

        assertEquals(balanceFirstBefore + amount, balanceFirstAfter);
        assertEquals(balanceSecondBefore - amount, balanceSecondAfter);
    }

    @Test
    void shouldFindBugWhenTransferMoreThanBalance() {
        int balanceSecondBefore = dashboardPage.getCardBalance(1);
        int amount = balanceSecondBefore + 6500;
        var transferPage = dashboardPage.selectCardToTransfer(0);
        transferPage.makeTransfer(String.valueOf(amount), DataHelper.getSecondCardInfo());
        int balanceSecondAfter = dashboardPage.getCardBalance(1);
        assertEquals(balanceSecondBefore, balanceSecondAfter, "Баланс карты списания не должен меняться при превышении лимита");
    }
}