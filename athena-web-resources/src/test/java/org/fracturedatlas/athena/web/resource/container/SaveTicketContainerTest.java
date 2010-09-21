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
package org.fracturedatlas.athena.web.resource.container;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.fracturedatlas.athena.client.PTicket;
import org.fracturedatlas.athena.apa.model.BooleanTicketProp;
import org.fracturedatlas.athena.apa.model.DateTimeTicketProp;
import org.fracturedatlas.athena.apa.model.IntegerTicketProp;
import org.fracturedatlas.athena.apa.model.PropField;
import org.fracturedatlas.athena.apa.model.StringTicketProp;
import org.fracturedatlas.athena.apa.model.Ticket;
import org.fracturedatlas.athena.apa.model.ValueType;
import org.fracturedatlas.athena.web.util.BaseTixContainerTest;
import org.fracturedatlas.athena.web.util.JsonUtil;
import org.fracturedatlas.athena.util.date.DateUtil;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SaveTicketContainerTest extends BaseTixContainerTest {

    Gson gson = JsonUtil.getGson();
    String path = "/";

    public SaveTicketContainerTest() throws Exception {
        super();
    }

    @After
    public void teardownTickets() {
        for (Ticket t : ticketsToDelete) {
            try {
                apa.deleteTicket(t);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        for (PropField pf : propFieldsToDelete) {
            try {
                apa.deletePropField(pf);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    @Test
    public void testCreateTicketWithNoProps() {
        Ticket t = new Ticket();
        t.setName("ticket");
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);
        String updatedTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(updatedTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);
        apa.deleteTicket(savedPTicket.getId());
    }

    @Test
    public void testCreateThenUpdateNoPropsNoChange() {
        Ticket t = new Ticket();
        t.setName("ticket");
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);
        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);

        String updatedTicketJson = tix.path(path).type("application/json").post(String.class, gson.toJson(savedPTicket));
        PTicket updatedPTicket = gson.fromJson(updatedTicketJson, PTicket.class);
        assertTrue(savedPTicket.equals(updatedPTicket));
        assertTicketsEqual(t, updatedPTicket, false);

        apa.deleteTicket(savedPTicket.getId());
    }

    @Test
    public void testCreateThenUpdate2NoPropsNoChange() {
        Ticket t = new Ticket();
        t.setName("ticket");
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);
        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);

        tix.path(path).type("application/json").post(String.class, gson.toJson(savedPTicket));
        tix.path(path).type("application/json").post(String.class, gson.toJson(savedPTicket));
        tix.path(path).type("application/json").post(String.class, gson.toJson(savedPTicket));

        String updatedTicketJson = tix.path(path).type("application/json").post(String.class, gson.toJson(savedPTicket));
        PTicket updatedPTicket = gson.fromJson(updatedTicketJson, PTicket.class);
        assertTrue(savedPTicket.equals(updatedPTicket));
        assertTicketsEqual(t, updatedPTicket, false);

        apa.deleteTicket(savedPTicket.getId());

        for (PropField f : propFieldsToDelete) {
            f = apa.getPropField(f.getId());
            assertEquals(1, f.getTicketProps().size());
        }
    }

    @Test
    public void testCreateThenUpdateName() {
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);
        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);
        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, true);

        for (PropField f : propFieldsToDelete) {
            f = apa.getPropField(f.getId());
            assertEquals(1, f.getTicketProps().size());
        }
    }

    @Test
    public void testUpdateAddNewProp() {
        
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);

        PropField field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("FOO");
        field.setStrict(Boolean.FALSE);
        PropField pf = apa.savePropField(field);
        propFieldsToDelete.add(pf);

        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();
        pTicket.put("FOO", "FIGHTERS");

        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertEquals(pTicket, savedPTicket);

        for (PropField f : propFieldsToDelete) {
            f = apa.getPropField(f.getId());
            assertEquals(1, f.getTicketProps().size());
        }

    }

    //We're updating a ticket but only sending one property.  Other properties should remain
    @Test
    public void testUpdateAddNewPropButDontSendOtherProps() {
        
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);

        PropField field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("FOO");
        field.setStrict(Boolean.FALSE);
        PropField pf = apa.savePropField(field);
        propFieldsToDelete.add(pf);

        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();
        pTicket.put("FOO", "FIGHTERS");

        PTicket testPTicket = new PTicket();
        testPTicket.setName(pTicket.getName());
        testPTicket.setId(pTicket.getId());
        testPTicket.put("FOO", "FIGHTERS");
        String ticketJson = gson.toJson(testPTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertEquals(pTicket, savedPTicket);

        for (PropField f : propFieldsToDelete) {
            f = apa.getPropField(f.getId());
            assertEquals(1, f.getTicketProps().size());
        }

    }

    @Test
    public void testUpdateProp() {
        
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);

        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();
        pTicket.put("SEAT_NUMBER", "3009");
        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertEquals(pTicket, savedPTicket);

        for (PropField field : propFieldsToDelete) {
            field = apa.getPropField(field.getId());
            assertEquals(1, field.getTicketProps().size());
        }
    }

    @Test
    public void testUpdateAllProps() {
        
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);

        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();
        pTicket.put("SEAT_NUMBER", "3009");
        pTicket.put("SECTION", "JIMMY");
        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertEquals(pTicket, savedPTicket);

        for (PropField field : propFieldsToDelete) {
            field = apa.getPropField(field.getId());
            assertEquals(1, field.getTicketProps().size());
        }
    }

    @Test
    public void testUpdateTicketBlankRequest() {
        
        Ticket t = createSampleTicket(false);
        ticketsToDelete.add(t);

        String ticketJson = "";

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testUpdateTicketBadJson() {
        
        Ticket t = createSampleTicket(false);
        ticketsToDelete.add(t);

        String ticketJson = "{BAD_JSON:BAD}";

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testUpdateTicketNotExist() {
        
        Ticket t = createSampleTicket(false);
        ticketsToDelete.add(t);

        t.setName("updated ticket");
        PTicket pTicket = t.toClientTicket();
        pTicket.setId(40000L);

        String ticketJson = gson.toJson(pTicket);

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testUpdateDateTimeProp() throws Exception {
        
        Ticket t = createSampleTicket(false);

        PropField field = new PropField(ValueType.DATETIME, "PERFORMANCE", Boolean.FALSE);
        PropField pf = apa.savePropField(field);
        propFieldsToDelete.add(pf);

        DateTimeTicketProp prop = new DateTimeTicketProp(pf, DateUtil.parseDate("2010-09-19 08:00"));
        t.addTicketProp(prop);

        t = apa.saveTicket(t);
        ticketsToDelete.add(t);

        PTicket pTicket = t.toClientTicket();
        pTicket.put("PERFORMANCE", "2010-09-30 09:33");
        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertEquals(pTicket, savedPTicket);

        for (PropField f : propFieldsToDelete) {
            f = apa.getPropField(f.getId());
            assertEquals(1, f.getTicketProps().size());
        }
    }

    @Test
    public void testUpdateDateTimePropNotADate() throws Exception {
            
        Ticket t = createSampleTicket(false);

        PropField field = new PropField(ValueType.DATETIME, "PERFORMANCE", Boolean.FALSE);
        PropField pf = apa.savePropField(field);
        propFieldsToDelete.add(pf);

        DateTimeTicketProp prop = new DateTimeTicketProp(pf, DateUtil.parseDate("2010-09-19 08:00"));
        t.addTicketProp(prop);

        t = apa.saveTicket(t);
        ticketsToDelete.add(t);

        PTicket pTicket = t.toClientTicket();
        pTicket.put("PERFORMANCE", "NOT_A_DATE");
        String ticketJson = gson.toJson(pTicket);

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }


    @Test
    public void testUpdateIntegerPropNotAnInteger() {
        
        Ticket t = createSampleTicket(false);

        PropField field = new PropField(ValueType.INTEGER, "PERFORMANCE", Boolean.FALSE);
        PropField pf = apa.savePropField(field);
        propFieldsToDelete.add(pf);

        IntegerTicketProp prop = new IntegerTicketProp(pf, 9);
        t.addTicketProp(prop);

        t = apa.saveTicket(t);
        ticketsToDelete.add(t);

        PTicket pTicket = t.toClientTicket();
        pTicket.put("PERFORMANCE", "NaN");
        String ticketJson = gson.toJson(pTicket);

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testCreateTicketBadIntegerValue() {
        
        Ticket t = createSampleTicket(false);
        PTicket pTicket = t.toClientTicket();

        PropField pf = apa.savePropField(new PropField(ValueType.INTEGER, "FOO_INT", Boolean.FALSE));
        propFieldsToDelete.add(pf);
        pTicket.put("FOO_INT", "NaN");

        String ticketJson = gson.toJson(pTicket);

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testCreateTicketBadDateTimeValue() {
        
        Ticket t = createSampleTicket(false);
        PTicket pTicket = t.toClientTicket();

        PropField pf = apa.savePropField(new PropField(ValueType.DATETIME, "FOO_DATE", Boolean.FALSE));
        propFieldsToDelete.add(pf);
        pTicket.put("FOO_INT", "NaD");

        String ticketJson = gson.toJson(pTicket);

        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));
    }

    @Test
    public void testCreateTicketBadBooleanValue() {
        
        Ticket t = createSampleTicket(false);
        PTicket pTicket = t.toClientTicket();

        PropField pf = apa.savePropField(new PropField(ValueType.BOOLEAN, "FOO_BOOL", Boolean.FALSE));
        propFieldsToDelete.add(pf);
        pTicket.put("FOO_BOOL", "notabool");

        String ticketJson = gson.toJson(pTicket);
        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertEquals("false", savedPTicket.get("FOO_BOOL"));
        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        ticketsToDelete.add(savedTicket);
    }

    @Test
    public void testCreateTicket() {
        
        Ticket t = createSampleTicket(false);
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);


        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);

        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        assertTicketsEqual(savedTicket, savedPTicket);
        ticketsToDelete.add(savedTicket);
    }

    @Test
    public void testUpdateTicketUnknownField() {
        
        Ticket t = createSampleTicket(true);
        PTicket pTicket = t.toClientTicket();
        pTicket.put("BAD_FIELD", "BAD_FISH");

        String ticketJson = gson.toJson(pTicket);
        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));

        //make sure the ticket hasn't changed
        PTicket actualPTicket = apa.getTicket(t.getId()).toClientTicket();
        assertTicketsEqual(t, actualPTicket);
    }

    @Test
    public void testCreateTicketUnknownField() {
        
        Ticket t = createSampleTicket(false);
        PTicket pTicket = t.toClientTicket();
        pTicket.put("BAD_FIELD", "BAD_FISH");

        String ticketJson = gson.toJson(pTicket);
        ClientResponse response = tix.path(path).type("application/json").post(ClientResponse.class, ticketJson);
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(response.getStatus()));

        //make sure nothing got saved
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("SECTION", "ORCHESTRA");
        assertEquals(0, apa.findTickets(map).size());
        map = new HashMap<String, String>();
        map.put("SEAT_NUMBER", "3D");
        assertEquals(0, apa.findTickets(map).size());
    }

    @Test
    public void testCreateTicketBooleanProp() {
        Ticket t = new Ticket();
        t.setName("ticket");

        PropField field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("SEAT_NUMBER");
        field.setStrict(Boolean.FALSE);
        PropField pf = apa.savePropField(field);

        field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("SECTION");
        field.setStrict(Boolean.FALSE);
        PropField pf2 = apa.savePropField(field);

        IntegerTicketProp prop = new IntegerTicketProp();
        prop.setPropField(pf);
        prop.setValue(490);
        t.addTicketProp(prop);

        BooleanTicketProp prop2 = new BooleanTicketProp();
        prop2.setPropField(pf2);
        prop2.setValue(true);
        t.addTicketProp(prop2);

        
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);

        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        assertTicketsEqual(savedTicket, savedPTicket);

        ticketsToDelete.add(savedTicket);
        propFieldsToDelete.add(pf);
        propFieldsToDelete.add(pf2);
    }

    @Test
    public void testCreateTicketDateTimeProp() throws Exception {
        Ticket t = new Ticket();
        t.setName("ticket");

        PropField field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("PERFORMANCE");
        field.setStrict(Boolean.FALSE);
        PropField pf = apa.savePropField(field);

        field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("SECTION");
        field.setStrict(Boolean.FALSE);
        PropField pf2 = apa.savePropField(field);

        DateTimeTicketProp prop = new DateTimeTicketProp();
        prop.setPropField(pf);
        prop.setValue(DateUtil.parseDate("2010-09-19 08:00"));
        t.addTicketProp(prop);

        BooleanTicketProp prop2 = new BooleanTicketProp();
        prop2.setPropField(pf2);
        prop2.setValue(true);
        t.addTicketProp(prop2);

        
        PTicket pTicket = t.toClientTicket();

        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        assertTicketsEqual(t, savedPTicket, false);

        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        assertTicketsEqual(savedTicket, savedPTicket);

        ticketsToDelete.add(savedTicket);
        propFieldsToDelete.add(pf);
        propFieldsToDelete.add(pf2);
    }

    @Test
    public void testCreateTicketTenProps() throws Exception {

        PropField seatNumberField = apa.savePropField(new PropField(ValueType.INTEGER, "SEAT_NUMBER", Boolean.FALSE));
        PropField sectionField = apa.savePropField(new PropField(ValueType.STRING, "SECTION", Boolean.FALSE));
        PropField soldField = apa.savePropField(new PropField(ValueType.BOOLEAN, "SOLD", Boolean.FALSE));
        PropField tierField = apa.savePropField(new PropField(ValueType.STRING, "TIER", Boolean.FALSE));
        PropField priceField = apa.savePropField(new PropField(ValueType.STRING, "PRICE", Boolean.FALSE));
        PropField performanceField = apa.savePropField(new PropField(ValueType.DATETIME, "PERFORMANCE", Boolean.FALSE));
        PropField venueField = apa.savePropField(new PropField(ValueType.STRING, "VENUE", Boolean.FALSE));
        PropField eventField = apa.savePropField(new PropField(ValueType.STRING, "EVENT", Boolean.FALSE));
        PropField lockedField = apa.savePropField(new PropField(ValueType.BOOLEAN, "LOCKED", Boolean.FALSE));
        PropField redeemedField = apa.savePropField(new PropField(ValueType.BOOLEAN, "REDEEMED", Boolean.FALSE));

        propFieldsToDelete.add(seatNumberField);
        propFieldsToDelete.add(sectionField);
        propFieldsToDelete.add(soldField);
        propFieldsToDelete.add(tierField);
        propFieldsToDelete.add(priceField);
        propFieldsToDelete.add(performanceField);
        propFieldsToDelete.add(venueField);
        propFieldsToDelete.add(eventField);
        propFieldsToDelete.add(lockedField);
        propFieldsToDelete.add(redeemedField);

        
        PTicket pTicket = new PTicket();

        pTicket.setName("ticket10");
        pTicket.getProps().put(seatNumberField.getName(), "34");
        pTicket.getProps().put(sectionField.getName(), "CCC");
        pTicket.getProps().put(soldField.getName(), "false");
        pTicket.getProps().put(tierField.getName(), "GOLD");
        pTicket.getProps().put(priceField.getName(), "3000");
        pTicket.getProps().put(performanceField.getName(), "2010-12-12 04:00");
        pTicket.getProps().put(venueField.getName(), "Everyman Theater");
        pTicket.getProps().put(eventField.getName(), "World Tour 2004");
        pTicket.getProps().put(lockedField.getName(), "false");
        pTicket.getProps().put(redeemedField.getName(), "false");

        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        pTicket.setId(savedPTicket.getId());
        assertTrue(pTicket.equals(savedPTicket));

        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        assertTicketsEqual(savedTicket, savedPTicket);

        ticketsToDelete.add(savedTicket);
    }

    //@Test
    //Disabling this so that it doesn't run during the normal "mvn test" cycle
    //It does indeed work, though.
    public void testCreateManyProps() throws Exception {
        //the actual number of props sent will be 4*NUMBER_OF_PROPS.  Bad name.
        final Integer NUMBER_OF_PROPS = 1000;

        List<PropField> propFields = new ArrayList<PropField>();

        
        PTicket pTicket = new PTicket();

        pTicket.setName("ticket " + UUID.randomUUID().toString().substring(3, 8));

        for (int i = 0; i < NUMBER_OF_PROPS; i++) {
            PropField randomField = apa.savePropField(new PropField(
                    ValueType.STRING,
                    UUID.randomUUID().toString().substring(2, 8),
                    Boolean.FALSE));
            propFields.add(randomField);
            propFieldsToDelete.add(randomField);

            pTicket.getProps().put(randomField.getName(), UUID.randomUUID().toString().substring(2, 8));
        }


        GregorianCalendar cal = new GregorianCalendar();
        for (int i = 0; i < NUMBER_OF_PROPS; i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            PropField randomField = apa.savePropField(new PropField(
                    ValueType.DATETIME,
                    UUID.randomUUID().toString().substring(3, 18),
                    Boolean.FALSE));
            propFields.add(randomField);
            propFieldsToDelete.add(randomField);

            pTicket.getProps().put(randomField.getName(), DateUtil.formatDate(cal.getTime()));
        }

        for (int i = 0; i < NUMBER_OF_PROPS; i++) {
            Random random = new Random();
            PropField randomField = apa.savePropField(new PropField(
                    ValueType.INTEGER,
                    UUID.randomUUID().toString().substring(3, 18),
                    Boolean.FALSE));
            propFields.add(randomField);
            propFieldsToDelete.add(randomField);

            pTicket.getProps().put(randomField.getName(), Integer.toString(random.nextInt()));
        }

        for (int i = 0; i < NUMBER_OF_PROPS; i++) {
            Boolean flipper = Boolean.TRUE;
            PropField randomField = apa.savePropField(new PropField(
                    ValueType.BOOLEAN,
                    UUID.randomUUID().toString().substring(3, 18),
                    Boolean.FALSE));
            propFields.add(randomField);
            propFieldsToDelete.add(randomField);

            pTicket.getProps().put(randomField.getName(), Boolean.toString(flipper));
            flipper = !flipper;
        }

        String ticketJson = gson.toJson(pTicket);

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());
        pTicket.setId(savedPTicket.getId());
        assertTrue(pTicket.equals(savedPTicket));

        Ticket savedTicket = apa.getTicket(savedPTicket.getId());
        assertTicketsEqual(savedTicket, savedPTicket);

        ticketsToDelete.add(savedTicket);
    }

    @Test
    public void testCreateThenUpdateDupeProp() {
        
        Ticket t = createSampleTicket(true);
        ticketsToDelete.add(t);
        PTicket pTicket = t.toClientTicket();

        String ticketJson = "{'id':'"+pTicket.getId()+"','name':'updated ticket','props':{'SECTION':'ORCHESTRA','SEAT_NUMBER':'3D','SEAT_NUMBER':'34F'}}";

        String createdTicketJson = tix.path(path).type("application/json").post(String.class, ticketJson);
        PTicket savedPTicket = gson.fromJson(createdTicketJson, PTicket.class);
        assertNotNull(savedPTicket.getId());

        assertEquals(2, savedPTicket.getProps().size());
        assertEquals("34F", savedPTicket.get("SEAT_NUMBER"));
        assertEquals("ORCHESTRA", savedPTicket.get("SECTION"));
    }

    public Ticket createSampleTicket(Boolean saveItToo) {
        Ticket t = new Ticket();
        t.setName("ticket");

        PropField field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("SEAT_NUMBER");
        field.setStrict(Boolean.FALSE);
        PropField pf = apa.savePropField(field);

        field = new PropField();
        field.setValueType(ValueType.STRING);
        field.setName("SECTION");
        field.setStrict(Boolean.FALSE);
        PropField pf2 = apa.savePropField(field);

        StringTicketProp prop = new StringTicketProp();
        prop.setPropField(pf);
        prop.setValue("3D");
        t.addTicketProp(prop);

        StringTicketProp prop2 = new StringTicketProp();
        prop2.setPropField(pf2);
        prop2.setValue("ORCHESTRA");
        t.addTicketProp(prop2);

        if (saveItToo) {
            t = apa.saveTicket(t);
            ticketsToDelete.add(t);
        }

        propFieldsToDelete.add(pf);
        propFieldsToDelete.add(pf2);

        return t;
    }
}