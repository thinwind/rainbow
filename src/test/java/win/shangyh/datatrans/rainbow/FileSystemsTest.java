/* 
 * Copyright 2024 Shang Yehua <niceshang@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package win.shangyh.datatrans.rainbow;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;

import org.junit.jupiter.api.Test;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-08  09:01
 *
 */
public class FileSystemsTest {
    
    @Test
    public void testFileAttributes() {
        FileSystem fs = FileSystems.getDefault();
        fs.supportedFileAttributeViews().forEach(System.out::println);
        java.nio.file.Files.lines(null, null)
    }
    
    @Test
    public void testSupportsFileAttributes(){
        FileSystem fs = FileSystems.getDefault();
        for (FileStore store : fs.getFileStores()) {
            System.out.println(store.name());
            System.out.println(store.supportsFileAttributeView("basic"));
            System.out.println(store.supportsFileAttributeView("posix"));
            System.out.println(store.supportsFileAttributeView("dos"));
            System.out.println(store.supportsFileAttributeView("user"));
            System.out.println(store.supportsFileAttributeView(FileOwnerAttributeView.class));
            System.out.println(store.supportsFileAttributeView(AclFileAttributeView.class));
            System.out.println(store.supportsFileAttributeView(UserDefinedFileAttributeView.class));
        }
    }
    
    @Test
    public void testBasicFileAttributes() throws IOException {
        Path path = Paths.get("/Users/shangyh/Playground/rainbow", "pom.xml.sl");
        BasicFileAttributes attrs=null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("size: " + attrs.size());
        System.out.println("creationTime: " + attrs.creationTime());
        System.out.println("lastAccessTime: " + attrs.lastAccessTime());
        System.out.println("lastModifiedTime: " + attrs.lastModifiedTime());
        
        System.out.println("isDirectory: " + attrs.isDirectory());
        System.out.println("isOther: " + attrs.isOther());
        System.out.println("isRegularFile: " + attrs.isRegularFile());
        System.out.println("isSymbolicLink: " + attrs.isSymbolicLink());
       
    }
    
    @Test
    public void testUpdateFileAttributes() throws IOException{
        Path path = Paths.get("/Users/shangyh/Playground/rainbow", "pom.xml");
        long time = System.currentTimeMillis();
        FileTime fileTime = FileTime.fromMillis(time);
        var basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
        basicView.setTimes(fileTime, fileTime, fileTime);
        
        FileTime lastModifiedTime = (FileTime) Files.getAttribute(path, "basic:lastModifiedTime", LinkOption.NOFOLLOW_LINKS);
        System.out.println(lastModifiedTime);
    }
    
    @Test
    public void testFileOwnerAttribute() throws IOException {
        Path path = Paths.get("/Users/shangyh/Playground/rainbow", "pom.xml");
        FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
        System.out.println(ownerView.getOwner());
    }
    
    @Test
    public void testPosixFileAttributes() {
        Path path = Paths.get("/Users/shangyh/Playground/rainbow", "pom.xml");
        PosixFileAttributes attrs=null;
        try {
            attrs = Files.readAttributes(path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("owner: " + attrs.owner());
        System.out.println("group: " + attrs.group());
        System.out.println("permissions: " + attrs.permissions());
    }
    
    @Test
    public void testTmpFile() throws IOException {
        Path tmp1 = Files.createTempDirectory(null);
        System.out.println(tmp1);
        Path tmp2 = Files.createTempDirectory("rainbow_");
        System.out.println(tmp2);
        
        var tmpLocation = System.getProperty("java.io.tmpdir");
        System.out.println(tmpLocation);
    }
    
    @Test
    public void testTraverse() {
        Path path = Paths.get("/Users/shangyh/Playground/rainbow");
        try {
            Files.walk(path).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testTraverse2() {
        Path path = Paths.get("/Users/shangyh/Playground/rainbow");
        try {
            Files.walkFileTree(path, new ListTree());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ListTree extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if(Files.isDirectory(file)){
            System.out.println(file);
        }
        return FileVisitResult.CONTINUE;
    }
    
    // @Override
    // public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    //     System.out.println(dir);
    //     return FileVisitResult.CONTINUE;
    // }
    
    // @Override
    // public FileVisitResult visitFileFailed(Path file, IOException exc) {
    //     System.out.println(file);
    //     return FileVisitResult.CONTINUE;
    // }
    
    // @Override
    // public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    //     System.out.println("Visiting directory: " + dir);
    //     return FileVisitResult.CONTINUE;
    // }
}
