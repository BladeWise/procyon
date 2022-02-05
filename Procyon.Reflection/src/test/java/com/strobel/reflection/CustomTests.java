package com.strobel.reflection;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CustomTests {

    @Test
    public void testFieldsWithConcreteGenericType() {
        final FieldInfo field = Type.of(TestObject.class).getField("items");
        assertEquals(Type.of(List.class).makeGenericType(Types.Double), field.getFieldType());
        final FieldInfo genericField = Type.of(GenericTestObject.class).getField("items");
        assertEquals(Type.of(List.class), genericField.getFieldType().getGenericTypeDefinition());
    }

    @Test
    public void genericParameterArrayTypesDoNotCollideWithObjectArrayInTypeCache() {
        final Type<MyClass> c1 = Type.of(MyClass.class); // Load class with method having T[] parameter
        c1.getMethods();  // Load methods of class with method having generic T[]
        final Type<MyOtherClass> c2 = Type.of(MyOtherClass.class); // Load class with method having Object[] parameter
        final Type<Object[]> objectArrayType = Types.Object.makeArrayType(); // Prepare Object[] type for comparison
        final Type<Object[]> objectArrayType2 = Types.Object.makeArrayType(); // Prepare Object[] type for comparison
        final MethodInfo method = c2.getMethod("getFirstOrNull", objectArrayType); // Load specific method having Object[] parameter
        final Type<?> methodParameterType = method.getParameters().getParameterTypes().get(0); // Retrieve method parameter type (expected to be Object[])
        assertEquals(objectArrayType, methodParameterType); // The method parameter type should match the expected Object[]
    }

    @Test
    public void concreteArrayTypesDoNotCollideWithObjectArrayInTypeCache() {
        final Type c1 = Type.of(MyOtherClass2.class);
        c1.getMethods();  // Load class with method having generic T[]
        final Type c2 = Type.of(MyOtherClass.class);
        c2.getMethods();
        final Type arrayType = Types.Object.makeArrayType();
        final MethodInfo method = c2.getMethod("getFirstOrNull", arrayType);
        final Type methodParameterType = method.getParameters().getParameterTypes().get(0);

        assertEquals(arrayType, methodParameterType);
    }

    @Test
    public void genericParameterArrayTypesDoNotCollideWithEachOtherInTypeCache() {
        final Type c1 = Type.of(MyClass.class);
        c1.getMethods();  // Load class with method having generic T[]
        final Type c2 = Type.of(MyClass2.class);
        c2.getMethods();
        final Type objectArrayType = Types.Object.makeArrayType();
        final Type c1ParameterType = c1.getMethod("enumerate", objectArrayType).getParameters().getParameterTypes().get(0);
        final Type c2ParameterType = c2.getMethod("enumerate", objectArrayType).getParameters().getParameterTypes().get(0);
        assertEquals(c2ParameterType.getElementType().makeArrayType(), c2ParameterType);
    }

    @Test
    public void methodHashCodeDoNotCollide()
    {
        final MethodInfo m1 = Type.of(Math.class).getMethod("abs", Types.Integer);
        final MethodInfo m2 = Type.of(Math.class).getMethod("abs", Types.Long);
        assertNotEquals(m1, m2);
        assertNotEquals(m1.hashCode(), m2.hashCode());
    }

    private static class MyClass {
        public static <T> Iterable<T> enumerate(final T[] items) {
            return Arrays.asList(items);
        }
    }

    private static class MyOtherClass {
        public static Object getFirstOrNull(final Object... values) {
            return values.length > 0 ? values[0] : null;
        }
    }

    private static class Item {
    }

    private static class MyOtherClass2 {
        public static Item getFirstOrNull(final Item... values) {
            return values.length > 0 ? values[0] : null;
        }
    }

    private static class MyClass2 {
        public static <ITEM> Iterable<ITEM> enumerate(final ITEM[] items) {
            return Arrays.asList(items);
        }
    }

    private static final class TestObject extends GenericTestObject<Double> {
    }

    public static class GenericTestObject<T> {
        public List<T> items;

        public T sum(final List<T> values) {
            throw new UnsupportedOperationException();
        }
    }
}
