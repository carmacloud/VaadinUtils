/* Copyright (C) OnePub IP Pty Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Brett Sutton <bsutton@onepub.dev>, Jan 2022
 */

package au.com.vaadinutils.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.crud.CrudEntity;

public class JpaDslAbstractTest {

    @Test
    public void test() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        Reflections reflections = new Reflections("com");

        Set<Class<? extends CrudEntity>> subTypes = reflections.getSubTypesOf(CrudEntity.class);

        for (Class<? extends CrudEntity> clazz : subTypes) {
            if ("TblCarmaSystem".equals(clazz.getSimpleName()) || clazz == ChildCrudEntity.class) {
                continue;
            }
            try {
                System.out.println("Testing " + clazz.getSimpleName());
                CrudEntity test = clazz.getConstructor().newInstance();
                test.setId(3L);
                CrudEntity copied = JpaDslAbstract.copyEntityForQuery(test);
                if (clazz.getName().contains(".function.") || clazz.getName().contains(".storedprocedure.")
                        || clazz.getName().contains(".view.") || clazz == ChildCrudEntity.class) {
                    continue;
                }
                Assert.assertTrue(3L == copied.getId());
            } catch (Exception e) {
                if (clazz.getSimpleName().toLowerCase().startsWith("vw")) {
                    System.out.println("View -> " + e.getMessage());
                }
                if (!"This is a view!".equals(e.getMessage()) && !"View, not updateable".equals(e.getMessage())) {
                    throw e;
                }
                System.out.println(e.getMessage());
            }
        }

    }

    class TestEntity implements CrudEntity {
        Long id;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;

        }

        @Override
        public String getName() {
            return null;
        }

    }

    @Test
    public void testCopyEntityForQueryCollection() {
        List<TestEntity> testList = new LinkedList<>();
        TestEntity o1 = new TestEntity();
        o1.setId(1L);
        TestEntity o2 = new TestEntity();
        o2.setId(1L);
        TestEntity o3 = new TestEntity();
        o3.setId(1L);

        testList.add(o1);
        testList.add(o2);
        testList.add(o3);

        List<TestEntity> result = (List<TestEntity>) JpaDslAbstract.copyEntityForQueryCollection(testList);

        assertTrue(result.get(0).getId() == o1.getId());
        assertTrue(result.get(1).getId() == o2.getId());
        assertTrue(result.get(2).getId() == o3.getId());

        assertEquals(3, result.size());
    }

    @Test
    public void testCopyEntityForQueryCollectionNull() {

        Collection<Object> result = JpaDslAbstract.copyEntityForQueryCollection(null);
        Assert.assertNull(result);
    }

    @Test
    public void testCopyEntityForQueryNull() {

        Collection<Object> result = JpaDslAbstract.copyEntityForQuery(null);
        Assert.assertNull(result);
    }
}
