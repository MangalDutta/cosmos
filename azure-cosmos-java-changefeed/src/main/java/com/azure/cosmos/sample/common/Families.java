// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.sample.common;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;

import java.util.UUID;

public class Families {

    public static Family getFamily() {
        Family family = new Family();
        family.setId(UUID.randomUUID().toString());
        family.setLastName(RandomStringUtils.randomAlphabetic(10));
        family.setPartitionKey(family.getLastName());
        System.out.println("Persist family with Id "+family.getId()+ " and last name "+family.getLastName());
        return family;
    }

    public static Family getFamily(final String partitionKey) {
        Family family = new Family();
        family.setId(UUID.randomUUID().toString());
        family.setLastName(RandomStringUtils.randomAlphabetic(10));
        family.setPartitionKey(partitionKey);
        System.out.println("Persist family with Id "+family.getId()+ " and last name "+family.getLastName());
        return family;
    }

    public static Family getAndersenFamilyItem() {
        Family andersenFamily = new Family();
        andersenFamily.setId("" + System.currentTimeMillis());
        andersenFamily.setLastName("Andersen");
        andersenFamily.setPartitionKey("Andersen");

        Parent parent1 = new Parent();
        parent1.setFirstName("Thomas");

        Parent parent2 = new Parent();
        parent2.setFirstName("Mary Kay");

        andersenFamily.setParents(new Parent[] { parent1, parent2 });

        Child child1 = new Child();
        child1.setFirstName("Henriette Thaulow");
        child1.setGender("female");
        child1.setGrade(5);

        Pet pet1 = new Pet();
        pet1.setGivenName("Fluffy");

        child1.setPets(new Pet[] { pet1 });

        andersenFamily.setDistrict("WA5");
        Address address = new Address();
        address.setCity("Seattle");
        address.setCounty("King");
        address.setState("WA");

        andersenFamily.setAddress(address);
        andersenFamily.setRegistered(true);

        return andersenFamily;
    }

    public static Family getWakefieldFamilyItem() {
        Family wakefieldFamily = new Family();
        wakefieldFamily.setId("Wakefield-" + System.currentTimeMillis());
        wakefieldFamily.setLastName("Wakefield");
        wakefieldFamily.setPartitionKey("Wakefield");

        Parent parent1 = new Parent();
        parent1.setFamilyName("Wakefield");
        parent1.setFirstName("Robin");

        Parent parent2 = new Parent();
        parent2.setFamilyName("Miller");
        parent2.setFirstName("Ben");

        wakefieldFamily.setParents(new Parent[] { parent1, parent2 });

        Child child1 = new Child();
        child1.setFirstName("Jesse");
        child1.setFamilyName("Merriam");
        child1.setGrade(8);

        Pet pet1 = new Pet();
        pet1.setGivenName("Goofy");

        Pet pet2 = new Pet();
        pet2.setGivenName("Shadow");

        child1.setPets(new Pet[] { pet1, pet2 });

        Child child2 = new Child();
        child2.setFirstName("Lisa");
        child2.setFamilyName("Miller");
        child2.setGrade(1);
        child2.setGender("female");

        wakefieldFamily.setChildren(new Child[] { child1, child2 });

        Address address = new Address();
        address.setCity("NY");
        address.setCounty("Manhattan");
        address.setState("NY");

        wakefieldFamily.setAddress(address);
        wakefieldFamily.setDistrict("NY23");
        wakefieldFamily.setRegistered(true);
        return wakefieldFamily;
    }

    public static Family getJohnsonFamilyItem() {
        Family andersenFamily = new Family();
        andersenFamily.setId("Johnson-" + System.currentTimeMillis());
        andersenFamily.setLastName("Johnson");
        andersenFamily.setPartitionKey("Johnson");

        Parent parent1 = new Parent();
        parent1.setFirstName("John");

        Parent parent2 = new Parent();
        parent2.setFirstName("Lili");

        return andersenFamily;
    }
    
    public static Family getSmithFamilyItem() {
        Family andersenFamily = new Family();
        andersenFamily.setId("Smith-" + System.currentTimeMillis());
        andersenFamily.setLastName("Smith");
        andersenFamily.setPartitionKey("Smith");

        Parent parent1 = new Parent();
        parent1.setFirstName("John");

        Parent parent2 = new Parent();
        parent2.setFirstName("Cynthia");

        return andersenFamily;
    }
}
