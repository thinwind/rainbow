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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-15  14:25
 *
 */
public class PathTest {

    @Test
    public void testPaths1() {
        Path path = Paths.get("/home/ata/projects/a-project/a-file.txt").normalize();
        Path path2 = Paths.get("/home/ata/projects/./a-project/a-file.txt").normalize();
        System.out.println(path);
        System.out.println(path2);
    }

    @Test
    public void testPaths2() {
        Path path = Paths.get(System.getProperty("user.home"), "downloads", "game.exe");
        System.out.println(path);
        System.out.println(path.getFileName());
        Path path2 = Paths.get("/home");
        System.out.println(path2.getParent());
        System.out.println(path2.getParent().getParent());
    }
    
    @Test
    public void test3(){
        Path path = Paths.get("/home/ata/projects/a-project/a-file.txt");
        // name count
        System.out.println(path.getNameCount());
        // iterate the name
        for (int i = 0; i < path.getNameCount(); i++) {
            System.out.println("Element " + i + " is: " + path.getName(i));
        }
    }
    
    @Test
    public void testSubpath(){
        Path path = Paths.get("/home/ata/projects/a-project/a-file.txt");
        System.out.println(path.subpath(1, 3));
    }
    
    @Test
    public void testComparePath() throws IOException{
        Path path = Paths.get("/home/ata/projects/a-project/a-file.txt");
        Path path2 = Paths.get("/home/ata/projects/a-project/a-file.txt");
        System.out.println(Files.isSameFile(path, path2));
    }

    @Test
    public void splitTest(){
        String seprator=Pattern.quote("^|^");
        String row="^|^test1^|^描述1^|^10^|^2024-03-09 12:00:01^|^^|^^|^";

        String[] fields = row.split(seprator);
        int i=0;
        for (String field : fields) {
            System.out.println(i+++":"+field);
        }
    }
}
