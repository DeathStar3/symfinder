package visitors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SymfinderVisitorTest {

    @Test
    public void noPackageClass() {
        String className = "MyClass";
        assertEquals(className, SymfinderVisitor.getClassBaseName(className));
    }

    @Test
    public void packageClass() {
        String className = "org.MyClass";
        assertEquals(className, SymfinderVisitor.getClassBaseName(className));
    }

    @Test
    public void noPackageGenericClass() {
        String className = "MyPair<Integer,String>";
        assertEquals("MyPair", SymfinderVisitor.getClassBaseName(className));
    }
    @Test
    public void packageGenericClass() {
        String className = "org.MyPair<Integer,String>";
        assertEquals("org.MyPair", SymfinderVisitor.getClassBaseName(className));
    }
}