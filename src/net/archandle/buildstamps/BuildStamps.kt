/*
 * Copyright 2022 Archandle Studio
 *
 * Licensed to any and all persons under the I Don't Give A Single Fuck About
 * What Happens To This Code License, Version 1. Hopefully, whoever distributed
 * this code to you included a LICENSE text file in the root directory of this
 * project, containing the full terms and conditions of this license. If not, you
 * can obtain a copy of any Version of the License at:
 * https://www.archandle.net/IDGASFAWHTTC-License
 */

package net.archandle.buildstamps

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.io.IOException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fetches a revision/build number from a supplied properties file ([buildNumberPropertiesFile]).
 *
 * The revision fetched from this properties file is attached to a key named `buildStamps.build`.
 *
 * If this revision is an integer, automatically increments the build number and stores it
 * back to the properties file.
 *
 * Stores the fetched revision in the supplied property name ([revisionPropertyName]).
 * If not provided, defaults to `buildStamps.buildNumber`.
 *
 * If the revision cannot be fetched from the supplied file, the revision property will be set to `ERR`.
 */
@Mojo(name = "buildRevision", defaultPhase = LifecyclePhase.INITIALIZE)
class BuildRevisionMojo: AbstractMojo() {
	/**
	 * Path to revision properties file.
	 */
	@Parameter(defaultValue = "\${basedir}/buildNumber.props")
	private lateinit var buildNumberPropertiesFile: File

	/**
	 * Maven project property name, which is populated by the fetched revision.
	 */
	@Parameter(defaultValue = "buildStamps.buildNumber")
	private lateinit var revisionPropertyName: String

	@Parameter(defaultValue = "\${project}", required = true, readonly = true)
	private lateinit var project: MavenProject

	override fun execute() {
		val build: String = try {
			createPropsFile(buildNumberPropertiesFile)
			buildNumberPropertiesFile.inputStream().use { ins ->
				val props = Properties()
				props.load(ins)
				val revision = props.getAndIncrement(BUILD_KEY)
				buildNumberPropertiesFile.outputStream().use { outs ->
					props.store(outs, project.properties.getProperty("project.build.finalName"))
				}
				revision
			}
		} catch (ioe: IOException) {
			log.warn("Could not read/write buildNumber properties file (${buildNumberPropertiesFile}).")
			log.warn("Reason: $ioe")
			"ERR"
		}
		project.properties.setProperty(revisionPropertyName, build)
	}

	private fun createPropsFile(file: File) {
		if (file.exists()) return
		if (!file.parentFile.exists()) {
			file.parentFile.mkdirs()
		}
		file.createNewFile()
	}

	private fun Properties.getAndIncrement(key: String): String {
		return if (this.containsKey(key)) {
			val fetchedRevision = getProperty(key) as String
			val asInt = fetchedRevision.toIntOrNull() ?: 0
			setProperty(key, (asInt + 1).toString())
			fetchedRevision
		} else {
			setProperty(key, "1")
			"0"
		}
	}

	companion object {
		const val BUILD_KEY = "buildStamps.build"
	}
}

/**
 * Fetches a timestamp and formats it according to [timestampFormat].
 *
 * Stores the fetched timestamp in the supplied property name ([timestampPropertyName]).
 * If not provided, defaults to `buildStamps.timestamp`.
 *
 * If the timestamp cannot be resolved, the timestamp property will be set to `ERR`.
 */
@Mojo(name = "buildTimestamp", defaultPhase = LifecyclePhase.INITIALIZE)
class BuildTimestampsMojo: AbstractMojo() {
	/**
	 * Timestamp format as according to [DateTimeFormatter].
	 */
	@Parameter(defaultValue = "yyyy-MM-dd-HH-mm-ss")
	private lateinit var timestampFormat: String

	/**
	 * Maven project property name, which is populated by a timestamp constructed using the supplied [timestampFormat].
	 */
	@Parameter(defaultValue = "buildStamps.timestamp")
	private lateinit var timestampPropertyName: String

	@Parameter(defaultValue = "\${project}", required = true, readonly = true)
	private lateinit var project: MavenProject

	override fun execute() {
		val formatter = DateTimeFormatter.ofPattern(timestampFormat)
		try {
			val stamp = formatter.format(LocalDateTime.now())
			project.properties.setProperty(timestampPropertyName, stamp)
		} catch (dte: DateTimeException) {
			log.warn("DateTimeFormatter has thrown an exception. Timestamp not constructed.")
			log.warn("Reason: $dte")
			project.properties.setProperty(timestampPropertyName, "ERR")
		}
	}
}