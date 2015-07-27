package org.moscatocms.sbt

import sbt._
import Keys._
import org.moscatocms.liquibase.DbConfig
import sbt.classpath.ClasspathUtilities
import java.io.FilenameFilter
import java.util.regex.Pattern
import org.reflections.Reflections
import scala.collection.JavaConverters._
import org.reflections.util.ConfigurationBuilder
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import java.util.Arrays
import org.moscatocms.liquibase.LiquibaseRunner
import scala.collection.JavaConversions._
import org.moscatocms.doctype.ChangelogUtils._

object MoscatoDatabasePlugin extends AutoPlugin {
  
  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
  object autoImport {
    
    val moscatoGenerateSchema = taskKey[Unit]("Generate Moscato database schema.")

    val moscatoDbUrl = SettingKey[String]("moscato-db-url", "The DB URL.")
    val moscatoDbUsername = SettingKey[String]("moscato-db-username", "The DB username.")
    val moscatoDbPassword = SettingKey[String]("moscato-db-password", "The DB password.")
    val moscatoDbDriver = SettingKey[String]("moscato-db-driver", "The DB driver class name.")
    val moscatoChangelogDir = SettingKey[File]("moscato-changelog-dir", "The directory to write changelogs to.")
    
    // default values for the tasks and settings
    lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
      
      moscatoGenerateSchema in Compile := {
        implicit val log = streams.value.log
        val dbConfig = DbConfig(
            moscatoDbUrl.value,
            moscatoDbDriver.value,
            Some(moscatoDbUsername.value),
            Some(moscatoDbPassword.value)
          )

        val classPath = (dependencyClasspath in Compile).value
        val classLoader = ClasspathUtilities.toLoader(classPath map { _.data })
        
        val configBuilder = new ConfigurationBuilder().
            //setUrls(classPath map { _.data.toURI.toURL }).
            setUrls(ClasspathHelper.forPackage("org.moscatocms.changelog", classLoader)).
            setScanners(new ResourcesScanner)
        val reflections = new Reflections(configBuilder)
        val changelogPaths = reflections.getResources(Pattern.compile(".*\\.xml")).asScala.toSeq
        val changelog = generateCompleteChangelog(moscatoChangelogDir.value, changelogPaths)
        new LiquibaseRunner(dbConfig, classLoader).update(changelog)
      },
      
      moscatoDbUrl := "",
      moscatoDbDriver := "",
      moscatoDbUsername := "",
      moscatoDbPassword := "",
      
      moscatoChangelogDir := (resourceManaged in Compile).value / organization.value.replace(".", "/") / "changelog"
    )
    
  }

  import autoImport._

  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override lazy val projectSettings = baseSettings

}