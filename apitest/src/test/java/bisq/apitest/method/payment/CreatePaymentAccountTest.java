/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.apitest.method.payment;

import bisq.core.payment.AdvancedCashAccount;
import bisq.core.payment.AliPayAccount;
import bisq.core.payment.AustraliaPayid;
import bisq.core.payment.CashDepositAccount;
import bisq.core.payment.ChaseQuickPayAccount;
import bisq.core.payment.ClearXchangeAccount;
import bisq.core.payment.F2FAccount;
import bisq.core.payment.FasterPaymentsAccount;
import bisq.core.payment.HalCashAccount;
import bisq.core.payment.InteracETransferAccount;
import bisq.core.payment.JapanBankAccount;
import bisq.core.payment.MoneyBeamAccount;
import bisq.core.payment.MoneyGramAccount;
import bisq.core.payment.NationalBankAccount;
import bisq.core.payment.PerfectMoneyAccount;
import bisq.core.payment.PopmoneyAccount;
import bisq.core.payment.PromptPayAccount;
import bisq.core.payment.RevolutAccount;
import bisq.core.payment.SameBankAccount;
import bisq.core.payment.SepaAccount;
import bisq.core.payment.SepaInstantAccount;
import bisq.core.payment.SpecificBanksAccount;
import bisq.core.payment.SwishAccount;
import bisq.core.payment.TransferwiseAccount;
import bisq.core.payment.USPostalMoneyOrderAccount;
import bisq.core.payment.UpholdAccount;
import bisq.core.payment.WeChatPayAccount;
import bisq.core.payment.WesternUnionAccount;
import bisq.core.payment.payload.BankAccountPayload;
import bisq.core.payment.payload.CashDepositAccountPayload;
import bisq.core.payment.payload.SameBankAccountPayload;
import bisq.core.payment.payload.SpecificBanksAccountPayload;

import java.io.File;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import static bisq.apitest.Scaffold.BitcoinCoreApp.bitcoind;
import static bisq.apitest.config.BisqAppConfig.alicedaemon;
import static bisq.core.locale.CurrencyUtil.*;
import static bisq.core.payment.payload.PaymentMethod.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@Disabled
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
public class CreatePaymentAccountTest extends AbstractPaymentAccountTest {

    @BeforeAll
    public static void setUp() {
        try {
            setUpScaffold(bitcoind, alicedaemon);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    public void testCreateAdvancedCashAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, ADVANCED_CASH_ID);
        verifyEmptyForm(emptyForm,
                ADVANCED_CASH_ID,
                PROPERTY_NAME_ACCOUNT_NR);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, ADVANCED_CASH_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Advanced Cash Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "0000 1111 2222");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Advanced Cash Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        AdvancedCashAccount paymentAccount = (AdvancedCashAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountTradeCurrencies(getAllAdvancedCashCurrencies(), paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), paymentAccount.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateAliPayAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, ALI_PAY_ID);
        verifyEmptyForm(emptyForm,
                ALI_PAY_ID,
                PROPERTY_NAME_ACCOUNT_NR);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, ALI_PAY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Ali Pay Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "2222 3333 4444");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        AliPayAccount paymentAccount = (AliPayAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("CNY", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), paymentAccount.getAccountNr());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateAustraliaPayidAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, AUSTRALIA_PAYID_ID);
        verifyEmptyForm(emptyForm,
                AUSTRALIA_PAYID_ID,
                PROPERTY_NAME_BANK_ACCOUNT_NAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, AUSTRALIA_PAYID_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Australia Pay ID Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAY_ID, "123 456 789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_ACCOUNT_NAME, "Credit Union Australia");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Australia Pay ID Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        AustraliaPayid paymentAccount = (AustraliaPayid) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("AUD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_PAY_ID), paymentAccount.getPayid());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_ACCOUNT_NAME), paymentAccount.getBankAccountName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateCashDepositAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, CASH_DEPOSIT_ID);
        verifyEmptyForm(emptyForm,
                CASH_DEPOSIT_ID,
                PROPERTY_NAME_ACCOUNT_NR,
                PROPERTY_NAME_ACCOUNT_TYPE,
                PROPERTY_NAME_BANK_ID,
                PROPERTY_NAME_BANK_NAME,
                PROPERTY_NAME_BRANCH_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_EMAIL,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_HOLDER_TAX_ID,
                PROPERTY_NAME_NATIONAL_ACCOUNT_ID,
                PROPERTY_NAME_REQUIREMENTS);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, CASH_DEPOSIT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Cash Deposit Account");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "4444 5555 6666");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_TYPE, "Checking");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_ID, "0001");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_NAME, "BoF");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BRANCH_ID, "99-8888-7654");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "FR");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_EMAIL, "jean@johnson.info");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jean Johnson");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_TAX_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_NATIONAL_ACCOUNT_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_REQUIREMENTS, "Requirements...");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        CashDepositAccount paymentAccount = (CashDepositAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("EUR", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);

        CashDepositAccountPayload payload = (CashDepositAccountPayload) paymentAccount.getPaymentAccountPayload();
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), payload.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_TYPE), payload.getAccountType());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_ID), payload.getBankId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BRANCH_ID), payload.getBranchId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_EMAIL), payload.getHolderEmail());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), payload.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_TAX_ID), payload.getHolderTaxId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_NATIONAL_ACCOUNT_ID), payload.getNationalAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_REQUIREMENTS), payload.getRequirements());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateBrazilNationalBankAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, NATIONAL_BANK_ID);
        verifyEmptyForm(emptyForm,
                NATIONAL_BANK_ID,
                PROPERTY_NAME_ACCOUNT_NR,
                PROPERTY_NAME_ACCOUNT_TYPE,
                PROPERTY_NAME_BANK_NAME,
                PROPERTY_NAME_BRANCH_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_HOLDER_TAX_ID,
                PROPERTY_NAME_NATIONAL_ACCOUNT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, NATIONAL_BANK_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Banco do Brasil");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "456789-87");
        // No BankId is required for BR.
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_NAME, "Banco do Brasil");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BRANCH_ID, "456789-10");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "BR");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Joao da Silva");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_TAX_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_NATIONAL_ACCOUNT_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Banco do Brasil Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        NationalBankAccount paymentAccount = (NationalBankAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("BRL", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);

        BankAccountPayload payload = (BankAccountPayload) paymentAccount.getPaymentAccountPayload();
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), payload.getAccountNr());
        // When no BankId is required, getBankId() returns bankName.
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), payload.getBankId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BRANCH_ID), payload.getBranchId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), payload.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_TAX_ID), payload.getHolderTaxId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_NATIONAL_ACCOUNT_ID), payload.getNationalAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateChaseQuickPayAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, CHASE_QUICK_PAY_ID);
        verifyEmptyForm(emptyForm,
                CHASE_QUICK_PAY_ID,
                PROPERTY_NAME_EMAIL,
                PROPERTY_NAME_HOLDER_NAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, CHASE_QUICK_PAY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Quick Pay Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL, "johndoe@quickpay.com");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "John Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        ChaseQuickPayAccount paymentAccount = (ChaseQuickPayAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL), paymentAccount.getEmail());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateClearXChangeAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, CLEAR_X_CHANGE_ID);
        verifyEmptyForm(emptyForm,
                CLEAR_X_CHANGE_ID,
                PROPERTY_NAME_EMAIL_OR_MOBILE_NR,
                PROPERTY_NAME_HOLDER_NAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, CLEAR_X_CHANGE_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "USD Zelle Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL_OR_MOBILE_NR, "jane@doe.com");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jane Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Zelle Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        ClearXchangeAccount paymentAccount = (ClearXchangeAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL_OR_MOBILE_NR), paymentAccount.getEmailOrMobileNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateF2FAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, F2F_ID);
        verifyEmptyForm(emptyForm,
                F2F_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_CITY,
                PROPERTY_NAME_CONTACT,
                PROPERTY_NAME_EXTRA_INFO);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, F2F_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Conta Cara a Cara");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "BR");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_CITY, "Rio de Janeiro");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_CONTACT, "Freddy Beira Mar");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EXTRA_INFO, "So fim de semana");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        F2FAccount paymentAccount = (F2FAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("BRL", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_CITY), paymentAccount.getCity());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_CONTACT), paymentAccount.getContact());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EXTRA_INFO), paymentAccount.getExtraInfo());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateFasterPaymentsAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, FASTER_PAYMENTS_ID);
        verifyEmptyForm(emptyForm,
                FASTER_PAYMENTS_ID,
                PROPERTY_NAME_ACCOUNT_NR,
                PROPERTY_NAME_SORT_CODE);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, FASTER_PAYMENTS_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Faster Payments Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "9999 8888 7777");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SORT_CODE, "3127");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Faster Payments Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        FasterPaymentsAccount paymentAccount = (FasterPaymentsAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("GBP", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), paymentAccount.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SORT_CODE), paymentAccount.getSortCode());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateHalCashAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, HAL_CASH_ID);
        verifyEmptyForm(emptyForm,
                HAL_CASH_ID,
                PROPERTY_NAME_MOBILE_NR);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, HAL_CASH_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Hal Cash Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_MOBILE_NR, "798 123 456");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        HalCashAccount paymentAccount = (HalCashAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("EUR", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_MOBILE_NR), paymentAccount.getMobileNr());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateInteracETransferAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, INTERAC_E_TRANSFER_ID);
        verifyEmptyForm(emptyForm,
                INTERAC_E_TRANSFER_ID,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_EMAIL,
                PROPERTY_NAME_QUESTION,
                PROPERTY_NAME_ANSWER);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, INTERAC_E_TRANSFER_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Interac Transfer Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "John Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL, "john@doe.info");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_QUESTION, "What is my dog's name?");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ANSWER, "Fido");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Interac Transfer Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        InteracETransferAccount paymentAccount = (InteracETransferAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("CAD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL), paymentAccount.getEmail());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_QUESTION), paymentAccount.getQuestion());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ANSWER), paymentAccount.getAnswer());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateJapanBankAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, JAPAN_BANK_ID);
        verifyEmptyForm(emptyForm,
                JAPAN_BANK_ID,
                PROPERTY_NAME_BANK_NAME,
                PROPERTY_NAME_BANK_CODE,
                PROPERTY_NAME_BANK_BRANCH_CODE,
                PROPERTY_NAME_BANK_BRANCH_NAME,
                PROPERTY_NAME_BANK_ACCOUNT_NAME,
                PROPERTY_NAME_BANK_ACCOUNT_TYPE,
                PROPERTY_NAME_BANK_ACCOUNT_NUMBER);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, JAPAN_BANK_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Fukuoka Account");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_NAME, "Bank of Kyoto");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_CODE, "FKBKJPJT");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_BRANCH_CODE, "8100-8727");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_BRANCH_NAME, "Fukuoka Branch");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_ACCOUNT_NAME, "Fukuoka Account");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_ACCOUNT_TYPE, "Yen Account");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_ACCOUNT_NUMBER, "8100-8727-0000");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        JapanBankAccount paymentAccount = (JapanBankAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("JPY", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_CODE), paymentAccount.getBankCode());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), paymentAccount.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_BRANCH_CODE), paymentAccount.getBankBranchCode());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_BRANCH_NAME), paymentAccount.getBankBranchName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_ACCOUNT_NAME), paymentAccount.getBankAccountName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_ACCOUNT_TYPE), paymentAccount.getBankAccountType());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_ACCOUNT_NUMBER), paymentAccount.getBankAccountNumber());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateMoneyBeamAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, MONEY_BEAM_ID);
        verifyEmptyForm(emptyForm,
                MONEY_BEAM_ID,
                PROPERTY_NAME_ACCOUNT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, MONEY_BEAM_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Money Beam Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_ID, "MB 0000 1111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Money Beam Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        MoneyBeamAccount paymentAccount = (MoneyBeamAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("EUR", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_ID), paymentAccount.getAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateMoneyGramAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, MONEY_GRAM_ID);
        verifyEmptyForm(emptyForm,
                MONEY_GRAM_ID,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_EMAIL,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_STATE);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, MONEY_GRAM_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Money Gram Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "John Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL, "john@doe.info");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "US");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_STATE, "NY");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        MoneyGramAccount paymentAccount = (MoneyGramAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountTradeCurrencies(getAllMoneyGramCurrencies(), paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getFullName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL), paymentAccount.getEmail());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_STATE), paymentAccount.getState());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreatePerfectMoneyAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, PERFECT_MONEY_ID);
        verifyEmptyForm(emptyForm,
                PERFECT_MONEY_ID,
                PROPERTY_NAME_ACCOUNT_NR);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, PERFECT_MONEY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Perfect Money Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "PM 0000 1111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Perfect Money Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        PerfectMoneyAccount paymentAccount = (PerfectMoneyAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), paymentAccount.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreatePopmoneyAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, POPMONEY_ID);
        verifyEmptyForm(emptyForm,
                POPMONEY_ID,
                PROPERTY_NAME_ACCOUNT_ID,
                PROPERTY_NAME_HOLDER_NAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, POPMONEY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Pop Money Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_ID, "POPMONEY 0000 1111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jane Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        PopmoneyAccount paymentAccount = (PopmoneyAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_ID), paymentAccount.getAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreatePromptPayAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, PROMPT_PAY_ID);
        verifyEmptyForm(emptyForm,
                PROMPT_PAY_ID,
                PROPERTY_NAME_PROMPT_PAY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, PROMPT_PAY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Prompt Pay Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PROMPT_PAY_ID, "PP 0000 1111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Prompt Pay Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        PromptPayAccount paymentAccount = (PromptPayAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("THB", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_PROMPT_PAY_ID), paymentAccount.getPromptPayId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateRevolutAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, REVOLUT_ID);
        verifyEmptyForm(emptyForm,
                REVOLUT_ID,
                PROPERTY_NAME_USERNAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, REVOLUT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Revolut Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_USERNAME, "revolut123");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        RevolutAccount paymentAccount = (RevolutAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountTradeCurrencies(getAllRevolutCurrencies(), paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_USERNAME), paymentAccount.getUserName());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateSameBankAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, SAME_BANK_ID);
        verifyEmptyForm(emptyForm,
                SAME_BANK_ID,
                PROPERTY_NAME_ACCOUNT_NR,
                PROPERTY_NAME_ACCOUNT_TYPE,
                PROPERTY_NAME_BANK_NAME,
                PROPERTY_NAME_BRANCH_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_HOLDER_TAX_ID,
                PROPERTY_NAME_NATIONAL_ACCOUNT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, SAME_BANK_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Same Bank Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "000 1 4567");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_TYPE, "Checking");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_NAME, "HSBC");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BRANCH_ID, "111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "GB");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jane Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_TAX_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_NATIONAL_ACCOUNT_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Same Bank Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        SameBankAccount paymentAccount = (SameBankAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("GBP", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        SameBankAccountPayload payload = (SameBankAccountPayload) paymentAccount.getPaymentAccountPayload();
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), payload.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_TYPE), payload.getAccountType());
        // The bankId == bankName because bank id is not required in the UK.
        assertEquals(payload.getBankId(), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BRANCH_ID), payload.getBranchId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), payload.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_TAX_ID), payload.getHolderTaxId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_NATIONAL_ACCOUNT_ID), payload.getNationalAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateSepaInstantAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, SEPA_INSTANT_ID);
        verifyEmptyForm(emptyForm,
                SEPA_INSTANT_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_IBAN,
                PROPERTY_NAME_BIC);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, SEPA_INSTANT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Conta Sepa Instant");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "PT");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jose da Silva");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_IBAN, "909-909");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BIC, "909");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        SepaInstantAccount paymentAccount = (SepaInstantAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        verifyAccountSingleTradeCurrency("EUR", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_IBAN), paymentAccount.getIban());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BIC), paymentAccount.getBic());
        // bankId == bic
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BIC), paymentAccount.getBankId());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateSepaAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, SEPA_ID);
        verifyEmptyForm(emptyForm,
                SEPA_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_IBAN,
                PROPERTY_NAME_BIC);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, SEPA_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Conta Sepa");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "PT");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jose da Silva");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_IBAN, "909-909");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BIC, "909");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Conta Sepa Salt"));
        String jsonString = getCompletedFormAsJsonString();
        SepaAccount paymentAccount = (SepaAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        verifyAccountSingleTradeCurrency("EUR", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_IBAN), paymentAccount.getIban());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BIC), paymentAccount.getBic());
        // bankId == bic
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BIC), paymentAccount.getBankId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateSpecificBanksAccount(TestInfo testInfo) {
        // TODO Supporting set of accepted banks may require some refactoring
        //  of the SpecificBanksAccount and SpecificBanksAccountPayload classes, i.e.,
        //      public void setAcceptedBanks(String... bankNames) { ... }
        File emptyForm = getEmptyForm(testInfo, SPECIFIC_BANKS_ID);
        verifyEmptyForm(emptyForm,
                SPECIFIC_BANKS_ID,
                PROPERTY_NAME_ACCOUNT_NR,
                PROPERTY_NAME_ACCOUNT_TYPE,
                PROPERTY_NAME_BANK_NAME,
                PROPERTY_NAME_BRANCH_ID,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_HOLDER_TAX_ID,
                PROPERTY_NAME_NATIONAL_ACCOUNT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, SPECIFIC_BANKS_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Specific Banks Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "000 1 4567");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_TYPE, "Checking");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BANK_NAME, "HSBC");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_BRANCH_ID, "111");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "GB");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jane Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_TAX_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_NATIONAL_ACCOUNT_ID, "123456789");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        SpecificBanksAccount paymentAccount = (SpecificBanksAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("GBP", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        SpecificBanksAccountPayload payload = (SpecificBanksAccountPayload) paymentAccount.getPaymentAccountPayload();
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), payload.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_TYPE), payload.getAccountType());
        // The bankId == bankName because bank id is not required in the UK.
        assertEquals(payload.getBankId(), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BANK_NAME), payload.getBankName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_BRANCH_ID), payload.getBranchId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), payload.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_TAX_ID), payload.getHolderTaxId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_NATIONAL_ACCOUNT_ID), payload.getNationalAccountId());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateSwishAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, SWISH_ID);
        verifyEmptyForm(emptyForm,
                SWISH_ID,
                PROPERTY_NAME_MOBILE_NR,
                PROPERTY_NAME_HOLDER_NAME);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, SWISH_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Swish Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_MOBILE_NR, "+46 7 6060 0101");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Swish Acct Holder");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Swish Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        SwishAccount paymentAccount = (SwishAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("SEK", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_MOBILE_NR), paymentAccount.getMobileNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateTransferwiseAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, TRANSFERWISE_ID);
        verifyEmptyForm(emptyForm,
                TRANSFERWISE_ID,
                PROPERTY_NAME_EMAIL);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, TRANSFERWISE_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Transferwise Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL, "jan@doe.info");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        TransferwiseAccount paymentAccount = (TransferwiseAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountTradeCurrencies(getAllTransferwiseCurrencies(), paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL), paymentAccount.getEmail());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateUpholdAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, UPHOLD_ID);
        verifyEmptyForm(emptyForm,
                UPHOLD_ID,
                PROPERTY_NAME_ACCOUNT_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, UPHOLD_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Uphold Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_ID, "UA 9876");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored Uphold Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        UpholdAccount paymentAccount = (UpholdAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountTradeCurrencies(getAllUpholdCurrencies(), paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_ID), paymentAccount.getAccountId());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateUSPostalMoneyOrderAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, US_POSTAL_MONEY_ORDER_ID);
        verifyEmptyForm(emptyForm,
                US_POSTAL_MONEY_ORDER_ID,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_POSTAL_ADDRESS);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, US_POSTAL_MONEY_ORDER_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Bubba's Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Bubba");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_POSTAL_ADDRESS, "000 Westwood Terrace Austin, TX 78700");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        USPostalMoneyOrderAccount paymentAccount = (USPostalMoneyOrderAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getHolderName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_POSTAL_ADDRESS), paymentAccount.getPostalAddress());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateWeChatPayAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, WECHAT_PAY_ID);
        verifyEmptyForm(emptyForm,
                WECHAT_PAY_ID,
                PROPERTY_NAME_ACCOUNT_NR);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, WECHAT_PAY_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "WeChat Pay Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NR, "WC 1234");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, encodeToHex("Restored WeChat Pay Acct Salt"));
        String jsonString = getCompletedFormAsJsonString();
        WeChatPayAccount paymentAccount = (WeChatPayAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("CNY", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_ACCOUNT_NR), paymentAccount.getAccountNr());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_SALT), paymentAccount.getSaltAsHex());
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @Test
    public void testCreateWesternUnionAccount(TestInfo testInfo) {
        File emptyForm = getEmptyForm(testInfo, WESTERN_UNION_ID);
        verifyEmptyForm(emptyForm,
                WESTERN_UNION_ID,
                PROPERTY_NAME_HOLDER_NAME,
                PROPERTY_NAME_CITY,
                PROPERTY_NAME_STATE,
                PROPERTY_NAME_COUNTRY,
                PROPERTY_NAME_EMAIL);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_PAYMENT_METHOD_ID, WESTERN_UNION_ID);
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_ACCOUNT_NAME, "Western Union Acct");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_HOLDER_NAME, "Jane Doe");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_CITY, "Fargo");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_STATE, "North Dakota");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_COUNTRY, "US");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_EMAIL, "jane@doe.info");
        COMPLETED_FORM_MAP.put(PROPERTY_NAME_SALT, "");
        String jsonString = getCompletedFormAsJsonString();
        WesternUnionAccount paymentAccount = (WesternUnionAccount) createPaymentAccount(alicedaemon, jsonString);
        verifyUserPayloadHasPaymentAccountWithId(paymentAccount.getId());
        verifyAccountSingleTradeCurrency("USD", paymentAccount);
        verifyCommonFormEntries(paymentAccount);
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_HOLDER_NAME), paymentAccount.getFullName());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_CITY), paymentAccount.getCity());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_STATE), paymentAccount.getState());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_EMAIL), paymentAccount.getEmail());
        assertEquals(COMPLETED_FORM_MAP.get(PROPERTY_NAME_COUNTRY),
                Objects.requireNonNull(paymentAccount.getCountry()).code);
        log.debug("Deserialized {}: {}", paymentAccount.getClass().getSimpleName(), paymentAccount);
    }

    @AfterAll
    public static void tearDown() {
        tearDownScaffold();
    }
}
