/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rest_ds;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.restds.RestDsConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {CoreConfiguration.class, RestDsConfiguration.class, TestRestDsConfiguration.class})
@ExtendWith(SpringExtension.class)
public class RestDsFetchPlanTest {

    @Autowired
    DataManager dataManager;

    @Test
    void testWithoutFetchPlan() {
        Condition condition = LogicalCondition.and(
                PropertyCondition.equal("firstName", "Robert"),
                PropertyCondition.equal("lastName", "Taylor")
        );
        List<Customer> customers = dataManager.load(Customer.class).condition(condition).list();

        assertThat(customers).size().isEqualTo(1);

        Customer customer = customers.get(0);

        assertThat(customer.getRegion()).isNull();
    }

    @Test
    void testWithFetchPlan() {
        Condition condition = LogicalCondition.and(
                PropertyCondition.equal("firstName", "Robert"),
                PropertyCondition.equal("lastName", "Taylor")
        );
        List<Customer> customers = dataManager.load(Customer.class)
                .condition(condition)
                .fetchPlan("customer-with-region")
                .list();

        assertThat(customers).size().isEqualTo(1);

        Customer customer = customers.get(0);

        assertThat(customer.getRegion()).isNotNull();

        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-region")
                .one();

        assertThat(customer.getRegion()).isNotNull();
    }
}
