package com.example.auth.service

object Schema {

  import java.sql.Timestamp

  import slick.driver.PostgresDriver.api._

  case class LoginRow(userEmail: String,
                      userId: String,
                      salt      : String,
                      passwordHash: String,
                      creationTime: Option[Timestamp] = None,
                      modifiedTime: Option[Timestamp] = None)

  case class UserRole(userId: String,
                      role: String,
                      creationTime: Option[Timestamp] = None,
                      modifiedTime: Option[Timestamp] = None)

  case class Permission(role: String,
                        method: String,
                        path: String,
                        creationTime: Option[Timestamp] = None,
                        modifiedTime: Option[Timestamp] = None)

  class LoginTable(tag: Tag) extends Table[LoginRow](tag, "logins") {
    def user_email = column[String]("user_email", O.PrimaryKey)
    def user_id = column[String]("user_id")
    def salt = column[String]("salt")
    def password_hash = column[String]("password_hash")
    def creation_time = column[Option[Timestamp]]("creation_time")
    def modified_time = column[Option[Timestamp]]("modified_time")

    def * = (user_email, user_id, salt, password_hash, creation_time, modified_time) <>(LoginRow.tupled, LoginRow.unapply)
  }

  val loginTable = TableQuery[LoginTable]

  class UserRoleTable(tag: Tag) extends Table[UserRole](tag, "user_roles") {
    def user_id = column[String]("user_id", O.PrimaryKey)
    def role = column[String]("role", O.PrimaryKey)
    def creation_time = column[Option[Timestamp]]("creation_time")
    def modified_time = column[Option[Timestamp]]("modified_time")

    def * = (user_id, role, creation_time, modified_time) <>(UserRole.tupled, UserRole.unapply)
  }

  val roleTable = TableQuery[UserRoleTable]

  class PermissionTable(tag: Tag) extends Table[Permission](tag, "permissions") {
    def role = column[String]("role", O.PrimaryKey)
    def method = column[String]("method", O.PrimaryKey)
    def path = column[String]("path", O.PrimaryKey)
    def creation_time = column[Option[Timestamp]]("creation_time")
    def modified_time = column[Option[Timestamp]]("modified_time")

    def * = (role, method, path, creation_time, modified_time) <>(Permission.tupled, Permission.unapply)
  }

  val permissionTable = TableQuery[PermissionTable]
}
