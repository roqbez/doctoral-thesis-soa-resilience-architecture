package br.ufsc.gsigma.common.hash;

import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HashUtil {

	private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);

	private static Map<Class<?>, String[]> mapHashFields = new HashMap<Class<?>, String[]>();

	private static String[] getExcludeFields(Class<?> tmpClass) {

		Set<String> fieldsNotHash = new HashSet<String>();

		while (tmpClass != null && tmpClass != Object.class) {

			for (Field declaredField : tmpClass.getDeclaredFields())
				if (!fieldsNotHash.contains(declaredField.getName()) && declaredField.getAnnotation(NotHash.class) != null)
					fieldsNotHash.add(declaredField.getName());

			for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(tmpClass)) {

				Method getter = pd.getReadMethod();

				if (getter != null && !fieldsNotHash.contains(pd.getName()) && getter.getAnnotation(NotHash.class) != null)
					fieldsNotHash.add(pd.getName());

			}

			tmpClass = tmpClass.getSuperclass();
		}

		return fieldsNotHash.toArray(new String[fieldsNotHash.size()]);

	}

	public static String getHashFromBytes(byte[] bytes) {
		return DigestUtils.sha1Hex(String.valueOf(Arrays.hashCode(bytes)));
	}

	public static int getHash(Object o) {

		String[] excludeFields = mapHashFields.get(o.getClass());

		if (excludeFields == null) {
			synchronized (mapHashFields) {
				excludeFields = mapHashFields.get(o.getClass());
				if (excludeFields == null) {
					excludeFields = getExcludeFields(o.getClass());
					if (logger.isInfoEnabled())
						logger.info("Excluding attributes " + ArrayUtils.toString(excludeFields) + " for hash of class " + o.getClass().getName());
					mapHashFields.put(o.getClass(), excludeFields);
				}
			}
		}

		return HashCodeBuilder.reflectionHashCode(o, excludeFields);

	}

	public static int getHashFromList(List<Object> list) {
		HashCodeBuilder builder = new HashCodeBuilder();

		for (Object o : list) {
			builder.append(o);
		}

		return builder.toHashCode();

	}

	public static int getHashFromValues(Object... values) {
		HashCodeBuilder builder = new HashCodeBuilder();

		for (Object o : values) {
			builder.append(o);
		}

		return builder.toHashCode();

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.FIELD })
	public @interface NotHash {
	}

}
