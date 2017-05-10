package com.proxy.kiwi.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Dynamic class acts as a wrapper for objects which can be observed for changes
 * and bounded to values. A mirror of the JavaFX implementation, without requirements
 * relating to the JavaFX runtime.
 */
public class Dynamic<T> {

	@SuppressWarnings("rawtypes")
	static List<Dynamic> dynamicsList;
	
	@SuppressWarnings("rawtypes")
	/**
	 * Dynamics can attach to a particular object (represented as a hash code
	 * in string form), removing any other dynamics attached to the same object
	 * in the process.
	 */
	static HashMap<String, Dynamic> receivers;
	
	T value;
	List<BiConsumer<T,T>> changeHandlers;
	HashMap<String, BiConsumer<T,T>> objectBindings;
	boolean bound;
	
	static {
		receivers = new HashMap<>();
	}
	
	public Dynamic(T initial) {
		this.value = initial;
		this.bound = false;
		this.changeHandlers = new ArrayList<>();
		this.objectBindings = new HashMap<>();
	}
	
	
	public void accept(Object obj, BiConsumer<T,T> fn) {
		String objectString = objectString(obj);
		if (receivers.get(objectString) != null) {
			receivers.get(objectString).removeObjectBinding(objectString);
			receivers.remove(objectString);
		}
		addObjectBinding(objectString, fn);
		fn.accept(null, get());
	}
	
	private void removeObjectBinding(String objString) {
		objectBindings.remove(objString);
	}
	
	private void addObjectBinding(String objString, BiConsumer<T,T> fn) {
		objectBindings.put(objString, fn);
		receivers.put(objString, this);
	}
	
	public void onChange(BiConsumer<T,T> fn) {
		changeHandlers.add(fn);
	}
	
	public T get() {
		return value;
	}
	
	public void set(T newVal) {
		T oldVal = this.value;
		this.value = newVal;
		changeHandlers.forEach(handler -> {
			handler.accept(oldVal,newVal);
		});
		objectBindings.values().forEach(handler -> {
			handler.accept(oldVal, newVal);
		});
	}
	
	@SafeVarargs
	public final <S> void bind(Function<List<S>,T> binding, Dynamic<S>...dynamics) {
		Arrays.asList(dynamics).forEach(dynamic -> {
			dynamic.onChange((old,newVal) -> {
				List<S> values = Arrays.asList(dynamics).stream().map(Dynamic::get).collect(Collectors.toList());
				set(binding.apply(values));
			});
		});
		
		List<S> values = Arrays.asList(dynamics).stream().map(Dynamic::get).collect(Collectors.toList());
		set(binding.apply(values));
	}
	
	/** Take the hash code of the object, even if it implements the toString method **/
	private String objectString(Object obj) {
		return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
	}
}
