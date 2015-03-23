# Crawler

A simple webcrawling library built using Scala and Akka. Allows users to easily perform actions for each page found by a crawler

# Building

Requires [SBT](http://www.scala-sbt.org/) to build. As long as you have it installed, you can run the following to package the JARs and install them to your local Maven repo:

```
sbt clean publish
```

# Including

If you publish this library locally, you can include it in any Maven project like so:

```
<dependency>
  <groupId>com.monitorjbl</groupId>
  <artifactId>crawler</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

# Usage

To use, simply write an implementation of the `com.monitorjbl.crawler.actors.HttpActor` class:

```
public class MyActor extends HttpActor {

  @Override
  public boolean shouldBeRead(String url, Map<String, String> headers) {
    return true;
  }

  @Override
  public void read(String url, Map<String, String> headers, InputStream content) {
    System.out.println(url);
  }
}
```

Then, start the crawler up with your class:

```
Crawler crawler = new Crawler("http://repo1.maven.org/maven2/org/apache/directory/api/api-all/", MyActor.class, 10, 1);
crawler.crawl();
```