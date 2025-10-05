package cn.hubbo.test.unit;

import cn.hubbo.entity.auth.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class KtDemoTest {


    @SuppressWarnings("all")
    @Test
    public void testKtEntityNoArgConstructor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        User user = new User();
        System.out.println(user);
        var cls = User.class;
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            System.out.println(constructor);
        }
        Constructor<User> constructor = cls.getDeclaredConstructor();
        var obj = constructor.newInstance();
        System.out.println(obj);
    }


}
