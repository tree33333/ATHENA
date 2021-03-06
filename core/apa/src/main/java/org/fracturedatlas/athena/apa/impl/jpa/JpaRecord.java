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
package org.fracturedatlas.athena.apa.impl.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fracturedatlas.athena.id.IdAdapter;
import org.fracturedatlas.athena.client.PTicket;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "TICKETS")
public class JpaRecord extends TixEntity implements Serializable {

    @Id
    @Type(type = "org.fracturedatlas.athena.apa.impl.LongUserType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Object id;

    @OneToMany(mappedBy = "ticket",
        targetEntity = TicketProp.class,
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL)
    Collection<TicketProp> ticketProps;

    String type;

    public JpaRecord() {
        ticketProps = new ArrayList<TicketProp>();
    }

    public JpaRecord(String type) {
        this.type = type;
        ticketProps = new ArrayList<TicketProp>();
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<TicketProp> getTicketProps() {
        return ticketProps;
    }

    public void setTicketProps(Collection<TicketProp> ticketProps) {
        this.ticketProps = ticketProps;
    }

    /**
     * Add a prop to this ticket.  If the prop exists on this ticket already, it will be replaced
     * @param prop
     */
    @Transient
    public void setTicketProp(TicketProp prop) throws Exception {
        String propName = prop.getPropField().getName();
        TicketProp existingProp = getTicketProp(propName);
        if(existingProp == null) {
            addTicketProp(prop);
        } else {
            existingProp.setValue(prop.getValue());
        }
    }

    /**
     * Add a prop to this ticket's props EVEN IF THIS PROP DUPLICATES AN EXISTING PROP
     * To avoid this, either use apa.savePropValue OR use ticket.setTicketProp
     *
     * @param prop
     */
    public void addTicketProp(TicketProp prop) {
        if (this.ticketProps == null) {
            this.ticketProps = new ArrayList<TicketProp>();
        }
        prop.setTicket(this);
        this.ticketProps.add(prop);
    }

    public TicketProp getTicketProp(String propName) {
        for(TicketProp prop : this.ticketProps) {
            if(propName.equals(prop.getPropField().getName())) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final JpaRecord other = (JpaRecord) obj;
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }

        if (!IdAdapter.isEqual(this.getId(), other.getId())) {
            return false;
        }

        if (ticketProps.size() != other.getTicketProps().size()) {
            return false;
        }
        TicketProp tempTp = null;
        for (TicketProp tp : ticketProps) {
            tempTp = other.getTicketProp(tp.getPropField().getName());
            if (tempTp == null) {
                return false;
            } else {
                if (!tp.equals(tempTp)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append(id).append(type);


        if (ticketProps != null) {
            for (TicketProp prop : ticketProps) {
                String propAndValue = "(" + prop.getPropField().getId() + ") " + prop.getPropField().getName() + ": [" + prop.getValueAsString() + "]";
                builder.append(propAndValue);
            }
        }
        return builder.toString();
    }

    public PTicket toClientTicket() {
        PTicket pTicket = new PTicket();

        if(this.getId() != null) {
            pTicket.setId(this.getId().toString());
        } else {
            pTicket.setId(null);
        }

        pTicket.setType(getType());

        for(TicketProp prop : this.getTicketProps()) {
            String propName = prop.getPropField().getName();
            if(propName.contains(":")) {
                pTicket.getSystemProps().putSingle(propName, prop.getValueAsString());
            } else {
                pTicket.getProps().add(propName, prop.getValueAsString());
            }
        }

        return pTicket;
    }
}
