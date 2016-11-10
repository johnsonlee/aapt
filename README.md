## Overview

AAPT is a Java library for Android assets manipulation, it supports read and write resource files. It also provides command line tools.

## Getting Started

### Using command line

- Dump Resource Table

    ```shell
    $ java -jar ./target/aapt-0.0.1-SNAPSHOT-jar-with-dependencies.jar dump resources resources.arsc
    ```

- Dump String Pool

    ```shell
    $ java -jar ./target/aapt-0.0.1-SNAPSHOT-jar-with-dependencies.jar dump strings resources.arsc
    ```

- Dump XML

    ```shell
    $ java -jar ./target/aapt-0.0.1-SNAPSHOT-jar-with-dependencies.jar dump xml AndroidManifest.xml
    ```

### Using library

- Dump Resource Table

    ```java
    ChunkParser parser = new ChunkParser();
parser.parse("resources.arsc").accept(new ResourceTableVisitor(System.out, true));    
    ```

- Dump String Pool

    ```java
    ChunkParser parser = new ChunkParser();
parser.parse("resources.arsc").accept(new StringPoolVisitor(System.out, true));
    ```

- Dump XML

    ```java
    ChunkParser parser = new ChunkParser();
parser.parse("AndroidManifest.xml").accept(new XmlVisitor(System.out, true));
    ```

- Write Resource Table

    ```java
    ChunkParser parser = new ChunkParser();
    ResourceTable arsc = parser.parse("resources.arsc");
    arsc.purge(0, 1, 2, 3, 4, 5); // retain the specified index of strings 

    ChunkOutputStream cos = new ChunkOutputStream(new FileOutputStream("resources.arsc.bak"));
    cos.write(arsc);
    cos.close();
    ```

- Parse & Generate Resource Symbols

    ```java
    SymbolParser parser = new SymbolParser();
    Symbols symbols = parser.parse("R.txt");
    Aapt.generateR("R.java", "com.example", symbols);
    ```


## Download

AAPT library is avaliable on [Maven Central Repository](http://search.maven.org/)

### Maven

```xml
<dependency>
  <groupId>com.sdklite</groupId>
  <artifactId>aapt</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Gradle

```gradle
compile 'com.sdklite:aapt:0.0.1'
```

## API Doc

Please see [AAPT API doc](http://aapt.sdklite.com).
