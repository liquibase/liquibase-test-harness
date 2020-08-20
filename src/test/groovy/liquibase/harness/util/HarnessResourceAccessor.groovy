package liquibase.harness.util

import liquibase.resource.ClassLoaderResourceAccessor

class HarnessResourceAccessor extends ClassLoaderResourceAccessor {

    HarnessResourceAccessor() throws Exception {
        super(new URLClassLoader(new URL[]{
                new File(System.getProperty("user.dir")).toURI().toURL(),
                new File("src/test/resources/").toURI().toURL(),
                new File(System.getProperty("java.io.tmpdir")).toURI().toURL(),
        }))
    }
}
