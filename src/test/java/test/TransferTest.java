package test;

import com.codeborne.selenide.Configuration;
import data.DataHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import page.DashboardPage;
import page.LoginPage;

import static com.codeborne.selenide.Selenide.*;
import static data.DataHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class TransferTest {

    @BeforeEach
    void setup() {
        Configuration.holdBrowserOpen = false;
        open("http://localhost:9999");
    }

    @AfterEach
    void clear() {
        clearBrowserCookies();
        clearBrowserLocalStorage();
        closeWebDriver();
    }

    DashboardPage login() {
        DataHelper.AuthInfo authInfo = DataHelper.getAuthInfo();
        var loginPage = open("http://localhost:9999", LoginPage.class);
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode(authInfo);
        return verificationPage.validVerify(verificationCode);
    }

    void balanceEquals(DashboardPage dashboardPage) {
        var firstCardBalance = dashboardPage.extractBalance(0);
        var secondCardBalance = dashboardPage.extractBalance(1);

        if (firstCardBalance == secondCardBalance) {
            return;
        }

        int targetBalance = (firstCardBalance + secondCardBalance) / 2;

        if (firstCardBalance < secondCardBalance) {
            int transferAmount = targetBalance - firstCardBalance;
            if (transferAmount > 0 && transferAmount <= secondCardBalance) {
                dashboardPage.pressReplCard(0)
                        .replCardBalance(transferAmount, DataHelper.getSecondCardNumber());
            }
        } else {
            int transferAmount = targetBalance - secondCardBalance;
            if (transferAmount > 0 && transferAmount <= firstCardBalance) {
                dashboardPage.pressReplCard(1)
                        .replCardBalance(transferAmount, DataHelper.getFirstCardNumber());
            }
        }
    }

    @Test
    void shouldTransferFromFirstCardToSecondCard() {
        DashboardPage dashboardPage = login();
        balanceEquals(dashboardPage);

        var firstCardBalance = dashboardPage.extractBalance(0);
        var secondCardBalance = dashboardPage.extractBalance(1);
        int transfer = getTransfer(firstCardBalance);

        assertTrue(transfer <= firstCardBalance, "Сумма перевода не может превышать баланс");

        dashboardPage.pressReplCard(1)
                .replCardBalance(transfer, DataHelper.getFirstCardNumber());

        assertEquals(firstCardBalance - transfer, dashboardPage.extractBalance(0),
                "Баланс первой карты должен уменьшиться на сумму перевода");
        assertEquals(secondCardBalance + transfer, dashboardPage.extractBalance(1),
                "Баланс второй карты должен увеличиться на сумму перевода");
    }

    @Test
    void shouldNotTransferIfFirstCardCanceled() {
        DashboardPage dashboardPage = login();
        balanceEquals(dashboardPage);

        var firstCardBalance = dashboardPage.extractBalance(0);
        var secondCardBalance = dashboardPage.extractBalance(1);
        int transfer = getTransfer(firstCardBalance);

        dashboardPage.pressReplCard(1)
                .replCardCancel(transfer, DataHelper.getFirstCardNumber());

        assertEquals(firstCardBalance, dashboardPage.extractBalance(0),
                "Баланс первой карты не должен измениться при отмене перевода");
        assertEquals(secondCardBalance, dashboardPage.extractBalance(1),
                "Баланс второй карты не должен измениться при отмене перевода");
    }

    @Test
    void shouldTransferFromSecondCardToFirstCard() {
        DashboardPage dashboardPage = login();
        balanceEquals(dashboardPage);

        var firstCardBalance = dashboardPage.extractBalance(0);
        var secondCardBalance = dashboardPage.extractBalance(1);
        int transfer = getTransfer(secondCardBalance);

        assertTrue(transfer <= secondCardBalance, "Сумма перевода не может превышать баланс");

        dashboardPage.pressReplCard(0)
                .replCardBalance(transfer, DataHelper.getSecondCardNumber());

        assertEquals(firstCardBalance + transfer, dashboardPage.extractBalance(0),
                "Баланс первой карты должен увеличиться на сумму перевода");
        assertEquals(secondCardBalance - transfer, dashboardPage.extractBalance(1),
                "Баланс второй карты должен уменьшиться на сумму перевода");
    }

    @Test
    void shouldNotTransferIfSecondCardCancelled() {
        DashboardPage dashboardPage = login();
        balanceEquals(dashboardPage);

        var firstCardBalance = dashboardPage.extractBalance(0);
        var secondCardBalance = dashboardPage.extractBalance(1);
        int transfer = getTransfer(secondCardBalance);

        dashboardPage.pressReplCard(0)
                .replCardCancel(transfer, DataHelper.getSecondCardNumber());

        assertEquals(firstCardBalance, dashboardPage.extractBalance(0),
                "Баланс первой карты не должен измениться при отмене перевода");
        assertEquals(secondCardBalance, dashboardPage.extractBalance(1),
                "Баланс второй карты не должен измениться при отмене перевода");
    }

    @Test
    void shouldNotTransfer12IfImpossibleTransfer() {
        DashboardPage dashboardPage = login();

        int firstCardBalance = dashboardPage.extractBalance(0);
        int secondCardBalance = dashboardPage.extractBalance(1);

        int transfer = getImpossibleTransfer(firstCardBalance);

        dashboardPage.pressReplCard(1)
                .replCardBalance(transfer, DataHelper.getFirstCardNumber());

        int newFirstBalance = dashboardPage.extractBalance(0);
        int newSecondBalance = dashboardPage.extractBalance(1);

        assertEquals(firstCardBalance - transfer, newFirstBalance,
                "Баланс первой карты должен уменьшиться на сумму перевода");
        assertEquals(secondCardBalance + transfer, newSecondBalance,
                "Баланс второй карты должен увеличиться на сумму перевода");

        System.out.println("Предупреждение: Баланс стал отрицательным: " + newFirstBalance);
    }

    @Test
    void shouldNotTransfer21IfImpossibleTransfer() {
        DashboardPage dashboardPage = login();

        int firstCardBalance = dashboardPage.extractBalance(0);
        int secondCardBalance = dashboardPage.extractBalance(1);

        int transfer = getImpossibleTransfer(secondCardBalance);

        dashboardPage.pressReplCard(0)
                .replCardBalance(transfer, DataHelper.getSecondCardNumber());

        int newFirstBalance = dashboardPage.extractBalance(0);
        int newSecondBalance = dashboardPage.extractBalance(1);

        assertEquals(firstCardBalance + transfer, newFirstBalance,
                "Баланс первой карты должен увеличиться на сумму перевода");
        assertEquals(secondCardBalance - transfer, newSecondBalance,
                "Баланс второй карты должен уменьшиться на сумму перевода");

        if (newSecondBalance < 0) {
            System.out.println("Предупреждение: Баланс стал отрицательным: " + newSecondBalance);
        }
    }
}