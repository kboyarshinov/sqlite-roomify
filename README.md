# sqlite-roomify

CLI tool to generate [Room][1] database [Entity][2] classes from SQLite schema to serve as a
basis to write further migration.

Features:
- Generate `Entity` class for each `CREATE TABLE` statement.
- Add indices to `Entity` classes for each `CREATE INDEX` statement for original table.
- Convert supported types and type affinities to Kotlin's basic types, except `NUMERIC` affinity.
- Support for `PRIMARY KEY` columns and nullability based on `NOT NULL` constraint.
- Generate `RoomDatabase` abstract class that references generated `Entity` classes.

Not supported:
- [Other table constraints][3] like `FOREIGN KEY`.
- [Other column constraints][4] like `COLLATE`.

## Compatibility

Generated classes aren't guaranteed to be 100% Room-compatible because Room itself doesn't
support everything SQLite can do.

For example `NUMERIC` type affinity is not supported by Room - columns of this affinity will be
be marked as `@Ignored` or added to `ignoredColumns` parameter along with other unrecognized
data types.

## Requirements

- JDK 19

## Usage

Example SQLite schema:

```sql
CREATE TABLE user(
    id      INT NOT NULL PRIMARY KEY,
    name    TEXT NOT NULL,
    age     INTEGER NOT NULL,
    email   TEXT
);

CREATE UNIQUE INDEX user_on_age ON user (age);
```

Run `sqlite-roomify` as:

```shell
./sqlite-roomify.sh tables.sql ./generated dev.test TestDatabase
```

`generated` folder would contain classes:

```kotlin
// dev.test.TestDatabase
@Database(
    entities = [
        User::class
    ],
    version = 1,
)
public abstract class TestDatabase : RoomDatabase()

// dev.test.tables.User
@Entity(
    tableName = "user",
    indices = [
        Index(name = "user_on_age", value = ["age"], unique = true)
    ],
)
public data class User(
    @PrimaryKey
    @ColumnInfo(
        name = "id",
        typeAffinity = ColumnInfo.INTEGER,
    )
    public val id: Int,
    @ColumnInfo(
        name = "name",
        typeAffinity = ColumnInfo.TEXT,
    )
    public val name: String,
    @ColumnInfo(
        name = "age",
        typeAffinity = ColumnInfo.INTEGER,
    )
    public val age: Int,
    @ColumnInfo(
        name = "email",
        typeAffinity = ColumnInfo.TEXT,
    )
    public val email: String?,
)
```

## Full list of options

```shell
./sqlite-roomify.sh --help
Usage: run [<options>] <input> <output> <package-name> <database-name>

Options:
  --list-all-ignored-columns=true|false  List all ignored columns in Entity's ignoredColumns parameter. 'true' by default.
  -h, --help                             Show this message and exit

Arguments:
  <input>          Input SQL Schema file
  <output>         Output directory for generated files
  <package-name>   Package name to use for generated files
  <database-name>  Name of generated database class
```

# License

    Copyright 2024 Kirill Boyarshinov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: https://developer.android.com/jetpack/androidx/releases/room
[2]: https://developer.android.com/reference/androidx/room/Entity
[3]: https://sqlite.org/lang_createtable.html
[4]: https://sqlite.org/syntax/column-constraint.html