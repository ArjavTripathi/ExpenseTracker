package com.chat.aj.expensetracker.common.Entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupEqualityTest {
    @Test
    void groupsWithTheSameIdAreEqualAcrossInstances() {
        Group first = new Group();
        first.setGroupId(4L);
        first.setName("Trip");
        Group second = new Group();
        second.setGroupId(4L);
        second.setName("Other name");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
