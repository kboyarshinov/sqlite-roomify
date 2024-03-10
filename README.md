# sql-roomify

CLI script to generate Room database Entity classes from SQL schema.

## Requirements

- JDK 19

## Usage

```shell
./sql-roomify.sh --help                                                                                                                                                                                                             [±main ●▴]
Usage: run [<options>] <input> <output> <package-name>

Options:
  -h, --help  Show this message and exit

Arguments:
  <input>         Input SQL Schema file
  <output>        Output folder for generated files
  <package-name>  Package name to use for generated files
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