package com.bank.api;

import com.bank.core.Account;
// Try uncommenting the line below later to see the module system block you!
// import com.bank.core.internal.InternalAudit;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Starting Bank API ---");

        Account myAccount = new Account();
        System.out.println(myAccount.getAccountDetails());

        // InternalAudit audit = new InternalAudit(); // This would fail to compile!
    }
}
