package com.yineng.ynmessager.util.address;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 注解类，主要用于项目里的各类地址获取
 * @author yhu
 * 使用方法：在要使用地址的类中，初始化时使用V8ContextAddress.bind(class)方法。
 * 然后就可以直接调用这个class里的url并且使用了
 * {@link V8ContextAddress}
 */
//目标类型为静态变量
@Target(ElementType.FIELD)
// 生命周期为运行时
@Retention(RetentionPolicy.RUNTIME)
public @interface V8AddressAnnotation {
	public enum ContextType {
		YNEDUT(0), WR(1), SMESIS(2), BPMX(3);

		private int code;

		ContextType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}
	}

	/**
	 * 上下文类型
	 *
	 * @return
	 */
	ContextType v8ContextType() default ContextType.YNEDUT;
}