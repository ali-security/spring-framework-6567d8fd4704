/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.expression.spel;

import org.springframework.core.SpringProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Configuration object for the SpEL expression parser.
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Andy Clement
 * @since 3.0
 * @see org.springframework.expression.spel.standard.SpelExpressionParser#SpelExpressionParser(SpelParserConfiguration)
 */
public class SpelParserConfiguration {

	/**
	 * Default maximum number of operations permitted during SpEL expression evaluation: {@value}.
	 * @since 5.3.39
	 * @see #SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME
	 */
	public static final int DEFAULT_MAX_OPERATIONS = 10_000;

	/** System property to configure the default compiler mode for SpEL expression parsers: {@value}. */
	public static final String SPRING_EXPRESSION_COMPILER_MODE_PROPERTY_NAME = "spring.expression.compiler.mode";

	/**
	 * System property to configure the default maximum number of operations permitted
	 * during SpEL expression evaluation: {@value}.
	 * @since 5.3.39
	 * @see #DEFAULT_MAX_OPERATIONS
	 */
	public static final String SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME = "spring.expression.maxOperations";


	private static final SpelCompilerMode defaultCompilerMode;

	static {
		String compilerMode = SpringProperties.getProperty(SPRING_EXPRESSION_COMPILER_MODE_PROPERTY_NAME);
		defaultCompilerMode = (compilerMode != null ?
				SpelCompilerMode.valueOf(compilerMode.toUpperCase()) : SpelCompilerMode.OFF);
	}


	private final SpelCompilerMode compilerMode;

	@Nullable
	private final ClassLoader compilerClassLoader;

	private final boolean autoGrowNullReferences;

	private final boolean autoGrowCollections;

	private final int maximumAutoGrowSize;

	private final int maximumOperations;


	/**
	 * Create a new {@code SpelParserConfiguration} instance with default settings.
	 */
	public SpelParserConfiguration() {
		this(null, null, false, false, Integer.MAX_VALUE);
	}

	/**
	 * Create a new {@code SpelParserConfiguration} instance.
	 * @param compilerMode the compiler mode for the parser
	 * @param compilerClassLoader the ClassLoader to use as the basis for expression compilation
	 */
	public SpelParserConfiguration(@Nullable SpelCompilerMode compilerMode, @Nullable ClassLoader compilerClassLoader) {
		this(compilerMode, compilerClassLoader, false, false, Integer.MAX_VALUE);
	}

	/**
	 * Create a new {@code SpelParserConfiguration} instance.
	 * @param autoGrowNullReferences if null references should automatically grow
	 * @param autoGrowCollections if collections should automatically grow
	 * @see #SpelParserConfiguration(boolean, boolean, int)
	 */
	public SpelParserConfiguration(boolean autoGrowNullReferences, boolean autoGrowCollections) {
		this(null, null, autoGrowNullReferences, autoGrowCollections, Integer.MAX_VALUE);
	}

	/**
	 * Create a new {@code SpelParserConfiguration} instance.
	 * @param autoGrowNullReferences if null references should automatically grow
	 * @param autoGrowCollections if collections should automatically grow
	 * @param maximumAutoGrowSize the maximum size that the collection can auto grow
	 */
	public SpelParserConfiguration(boolean autoGrowNullReferences, boolean autoGrowCollections, int maximumAutoGrowSize) {
		this(null, null, autoGrowNullReferences, autoGrowCollections, maximumAutoGrowSize);
	}

	/**
	 * Create a new {@code SpelParserConfiguration} instance.
	 * @param compilerMode the compiler mode that parsers using this configuration object should use
	 * @param compilerClassLoader the ClassLoader to use as the basis for expression compilation
	 * @param autoGrowNullReferences if null references should automatically grow
	 * @param autoGrowCollections if collections should automatically grow
	 * @param maximumAutoGrowSize the maximum size that the collection can auto grow
	 */
	public SpelParserConfiguration(@Nullable SpelCompilerMode compilerMode, @Nullable ClassLoader compilerClassLoader,
			boolean autoGrowNullReferences, boolean autoGrowCollections, int maximumAutoGrowSize) {

		this.compilerMode = (compilerMode != null ? compilerMode : defaultCompilerMode);
		this.compilerClassLoader = compilerClassLoader;
		this.autoGrowNullReferences = autoGrowNullReferences;
		this.autoGrowCollections = autoGrowCollections;
		this.maximumAutoGrowSize = maximumAutoGrowSize;
		this.maximumOperations = retrieveMaxOperations();
	}


	/**
	 * Return the compiler mode for parsers using this configuration object.
	 */
	public SpelCompilerMode getCompilerMode() {
		return this.compilerMode;
	}

	/**
	 * Return the ClassLoader to use as the basis for expression compilation.
	 */
	@Nullable
	public ClassLoader getCompilerClassLoader() {
		return this.compilerClassLoader;
	}

	/**
	 * Return {@code true} if {@code null} references should be automatically grown.
	 */
	public boolean isAutoGrowNullReferences() {
		return this.autoGrowNullReferences;
	}

	/**
	 * Return {@code true} if collections should be automatically grown.
	 */
	public boolean isAutoGrowCollections() {
		return this.autoGrowCollections;
	}

	/**
	 * Return the maximum size that a collection can auto grow.
	 */
	public int getMaximumAutoGrowSize() {
		return this.maximumAutoGrowSize;
	}

	/**
	 * Return the maximum number of operations permitted during SpEL expression
	 * evaluation.
	 * <p>The value is resolved from the {@link #SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME}
	 * JVM system property or Spring property when this configuration is created,
	 * falling back to {@link #DEFAULT_MAX_OPERATIONS}.
	 * @since 5.3.39
	 */
	public int getMaximumOperations() {
		return this.maximumOperations;
	}


	private static int retrieveMaxOperations() {
		String value = SpringProperties.getProperty(SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME);
		if (!StringUtils.hasText(value)) {
			return DEFAULT_MAX_OPERATIONS;
		}

		try {
			int maxOperations = Integer.parseInt(value.trim());
			Assert.isTrue(maxOperations > 0, "Value [" + maxOperations + "] for system property [" +
					SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME + "] must be positive");
			return maxOperations;
		}
		catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Failed to parse value for system property [" +
					SPRING_EXPRESSION_MAX_OPERATIONS_PROPERTY_NAME + "]: " + ex.getMessage(), ex);
		}
	}

}
