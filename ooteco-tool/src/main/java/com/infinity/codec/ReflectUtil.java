package com.infinity.codec;

import com.infinity.aop.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射帮助
 * 
 */
public class ReflectUtil {

    /**
     * 根据属名字，获取类的属
     * 
     * @param fieldName
     * @param clazz
     * @return
     * @throws Exception
     */
    public static Field getFieldByName(String fieldName, Class<?> clazz) throws Exception {
        Class<?> superClass = clazz;
        Field field = null;
        while (superClass != null && field == null) {
            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
            superClass = superClass.getSuperclass();
        }
        return field;
    }


    @SuppressWarnings("rawtypes")
    public static Method getMethodByName(String methodName, Class parameterTypes, Class<?> clazz) throws Exception {
        Class<?> superClass = clazz;
        Method method = null;
        while (superClass != null && method == null) {
            try {
                method = superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
            }
            superClass = superClass.getSuperclass();
        }
        return method;
    }


    /**
     * 获取类的泛型的class
     * 
     * @param c
     * @param index
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Class getGenericType(Class c, int index) {
        java.lang.reflect.Type genType = c.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        java.lang.reflect.Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }



    /**
     * object to long
     * 
     * @param obj
     * @return
     */
    public static long getLongValueForLongOrInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return (Long) obj;
        }
    }

    /**
     * 根据sql结果集和类型返回对应的类
     * 
     * @param type
     * @param rs
     * @param cloum
     * @return
     * @throws SQLException
     */
    public static Object getValueFormRsByType(java.lang.reflect.Type type, ResultSet rs, String cloum) throws SQLException {
        if (type == Long.class || type == long.class) {
            return rs.getLong(cloum);
        } else if (type == Integer.class || type == int.class) {
            return rs.getInt(cloum);
        } else if (type == Double.class || type == double.class) {
            return rs.getDouble(cloum);
        } else if (type == String.class) {
            return rs.getString(cloum);
        }
        throw new RuntimeException("不支持此类型");

    }

    static Map<Method, String[]> methodParamMap = new ConcurrentHashMap<Method, String[]>();

    /**
     * 根据method获取他所有的参数的参数名
     * 
     * @param m
     * @return
     */
    public static String[] getMethodParamNames(final Method m) {
        if (methodParamMap.get(m) != null) {
            return methodParamMap.get(m).clone();
        }
        final String[] paramNames = new String[m.getParameterTypes().length];
        final String n = m.getDeclaringClass().getName();
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassReader cr = null;
        try {
            cr = new ClassReader(n);
        } catch (IOException e) {
            // e.printStackTrace();
            try {
                InputStream in =
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(n.replace('.', '/') + ".class");
                cr = new ClassReader(in, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        cr.accept(new ClassVisitor(Opcodes.ASM4, cw) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                             final String[] exceptions) {
                final Type[] args = Type.getArgumentTypes(desc);
                if (!name.equals(m.getName()) || !sameType(args, m.getParameterTypes())) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
                // System.out.println(desc);
                MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM4, v) {
                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end,
                            int index) {
                        // System.out.println("name:" + name);
                        if (Modifier.isStatic(m.getModifiers()) || index > 0) {
                            for (int i = 0; i < paramNames.length; i++) {
                                if (paramNames[i] == null) {
                                    paramNames[i] = name;
                                    break;
                                }
                            }
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }
        }, 0);
        methodParamMap.put(m, paramNames);
        return paramNames.clone();

    }

    private static boolean sameType(Type[] types, Class<?>[] clazzes) {
        if (types.length != clazzes.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(clazzes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] sdg) {

        System.out.println(Arrays.asList(getMethodParamNames(ReflectUtil.class.getMethods()[2])));
    }

}
