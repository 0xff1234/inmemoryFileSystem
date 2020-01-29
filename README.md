# memoryFileSystem
##a coding test that implement a file system apis in memory

###interface definition
```java
public interface FileSystem {

    Iterable<String> ls();

              /**

              * Support nested paths, eg., /a/b/c

              */

              boolean mkdir(String path);

              /**

              * Return the absolute path

              */

              String touch(String path);

              /**

              * Support nested paths, return the absolute path

              */

              String cd(String path);

             

              String pwd();

            

              boolean rm(String path, boolean recursive);

 

}
```