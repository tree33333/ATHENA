/*

ATHENA Project: Management Tools for the Cultural Sector
Copyright (C) 2010, Fractured Atlas

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/

*/

package org.fracturedatlas.athena.payments.manager;

import com.sun.jersey.api.NotFoundException;
import org.fracturedatlas.athena.payments.model.Customer;
import org.fracturedatlas.athena.payments.processor.PaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;


public class CustomersManager {

    @Autowired
    PaymentProcessor processor;

    public Customer get(String id) {
        try{
            return processor.getCustomer(id);
        } catch (com.braintreegateway.exceptions.NotFoundException bnfe) {
            throw new NotFoundException("Customer with id [" + id + "] was not found");
        }
    }

    public org.fracturedatlas.athena.payments.model.Customer save(org.fracturedatlas.athena.payments.model.Customer customer) {
        return processor.saveCustomer(customer);
    }

    public void delete(String id) {
        processor.deleteCustomer(id);
    }
}
