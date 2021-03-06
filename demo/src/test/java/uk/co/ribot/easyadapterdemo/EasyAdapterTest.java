/*
 * Copyright (C) 2015 Ribot Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.ribot.easyadapterdemo;

import android.database.DataSetObserver;
import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import uk.co.ribot.easyadapter.BuildConfig;
import uk.co.ribot.easyadapter.EasyAdapter;
import uk.co.ribot.easyadapterdemo.util.DefaultConfig;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.co.ribot.easyadapterdemo.util.DataUtil.createPerson;
import static uk.co.ribot.easyadapterdemo.util.DataUtil.getSomePeople;

/**
 * This test is in the demo app so it can use the layout resources defined for the PersonViewHolder
 * Ideally it should be in the library module because is testing the EasyAdapter class.
 * However in order to create an EasyAdapter we need a view holder annotated with a
 * valid layout ID. At the moment it's not possible to define a resource layout in the test variant
 * and I didn't want to include resources in the library that are only used for testing.
 */

@RunWith(RobolectricGradleTestRunner.class)
// Needs to include package name in config here because of this issue
// https://github.com/robolectric/robolectric/issues/1623
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK,
        packageName = DefaultConfig.PACKAGE_NAME)
public class EasyAdapterTest {

    public EasyAdapter<Person> mEasyAdapter;
    public DataSetObserver mMockDataSetObserver;

    @Before public void setUp() {
        mEasyAdapter = new EasyAdapter<>(RuntimeEnvironment.application, PersonViewHolder.class);
        mMockDataSetObserver = mock(DataSetObserver.class);
        mEasyAdapter.registerDataSetObserver(mMockDataSetObserver);
    }

    @Test public void testGetCount() throws Exception {
        mEasyAdapter.getItems().addAll(Arrays.asList(
                createPerson("Person1"),
                createPerson("Person2")));
        assertEquals(2, mEasyAdapter.getCount());
    }

    @Test public void testGetItem() throws Exception {
        Person person1 = createPerson("Person1");
        Person person2 = createPerson("Person2");
        mEasyAdapter.getItems().addAll(Arrays.asList(person1, person2));
        assertEquals(person1, mEasyAdapter.getItem(0));
        assertEquals(person2, mEasyAdapter.getItem(1));
    }

    @Test public void testGetItems() throws Exception {
        List<Person> list = getSomePeople();
        EasyAdapter<Person> easyAdapter = new EasyAdapter<>(RuntimeEnvironment.application,
                PersonViewHolder.class, list);
        assertEquals(list, easyAdapter.getItems());
    }

    @Test public void testSetItems() throws Exception {
        List<Person> list = getSomePeople();
        assertEquals(0, mEasyAdapter.getItems().size());
        mEasyAdapter.setItems(list);
        assertEquals(list, mEasyAdapter.getItems());
        verify(mMockDataSetObserver).onChanged();
    }

    @Test public void testSetItemsWithoutNotifying() throws Exception {
        List<Person> list = getSomePeople();
        assertEquals(0, mEasyAdapter.getItems().size());
        mEasyAdapter.setItemsWithoutNotifying(list);
        assertEquals(list, mEasyAdapter.getItems());
        verify(mMockDataSetObserver, never()).onChanged();
    }

    @Test public void testAddItem() throws Exception {
        mEasyAdapter.getItems().addAll(getSomePeople());
        Person newPerson = createPerson("New Person");
        mEasyAdapter.addItem(newPerson);

        assertTrue(mEasyAdapter.getItems().contains(newPerson));
        verify(mMockDataSetObserver).onChanged();
    }

    @Test public void testRemoveItem() throws Exception {
        List<Person> items = getSomePeople();
        Person personToRemove = items.get(0);
        mEasyAdapter.getItems().addAll(items);
        boolean result = mEasyAdapter.removeItem(personToRemove);

        assertTrue(result);
        assertFalse(mEasyAdapter.getItems().contains(personToRemove));
        verify(mMockDataSetObserver).onChanged();
    }

    @Test public void testRemoveNonExistingItem() throws Exception {
        Person person = createPerson("Person1");
        boolean result = mEasyAdapter.removeItem(person);

        assertFalse(result);
        verify(mMockDataSetObserver, never()).onChanged();
    }

    @Test public void testAddItems() throws Exception {
        Person person1 = createPerson("Person1");
        Person person2 = createPerson("Person2");
        Person person3 = createPerson("Person3");
        mEasyAdapter.getItems().add(person1);
        boolean result = mEasyAdapter.addItems(Arrays.asList(person2, person3));

        assertTrue(result);
        assertEquals(3, mEasyAdapter.getCount());
        assertTrue(mEasyAdapter.getItems().contains(person2));
        assertTrue(mEasyAdapter.getItems().contains(person3));
        verify(mMockDataSetObserver).onChanged();
    }

    @Test public void testRemoveItems() throws Exception {
        Person person1 = createPerson("Person1");
        Person person2 = createPerson("Person2");
        Person person3 = createPerson("Person3");
        mEasyAdapter.getItems().addAll(Arrays.asList(person1, person2, person3));
        boolean result = mEasyAdapter.removeItems(Arrays.asList(person1, person3));

        assertTrue(result);
        assertEquals(1, mEasyAdapter.getCount());
        assertFalse(mEasyAdapter.getItems().contains(person1));
        assertTrue(mEasyAdapter.getItems().contains(person2));
        assertFalse(mEasyAdapter.getItems().contains(person3));
        verify(mMockDataSetObserver).onChanged();
    }

    @Test public void testRemoveNonExistingItems() throws Exception {
        Person person1 = createPerson("Person1");
        Person person2 = createPerson("Person2");
        boolean result = mEasyAdapter.removeItems(Arrays.asList(person1, person2));

        assertFalse(result);
        verify(mMockDataSetObserver, never()).onChanged();
    }

    @Test public void testGetView() throws Exception {
        List<Person> listPeople = getSomePeople();
        mEasyAdapter.getItems().addAll(listPeople);
        for (int position = 0; position < listPeople.size(); position++) {
            View view = mEasyAdapter.getView(position, null, null);
            TextView textViewName = (TextView) view.findViewById(R.id.text_view_name);
            assertEquals(listPeople.get(position).getName(), textViewName.getText().toString());
        }
    }

}
