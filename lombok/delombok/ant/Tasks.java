/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Location
 *  org.apache.tools.ant.Task
 *  org.apache.tools.ant.types.FileSet
 *  org.apache.tools.ant.types.Path
 *  org.apache.tools.ant.types.Reference
 *  org.apache.tools.ant.types.ResourceCollection
 */
package lombok.delombok.ant;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ResourceCollection;

class Tasks {
    Tasks() {
    }

    public static class Delombok
    extends Task {
        private File fromDir;
        private File toDir;
        private Path classpath;
        private Path sourcepath;
        private Path modulepath;
        private boolean verbose;
        private String encoding;
        private Path path;
        private List<Format> formatOptions = new ArrayList<Format>();
        private static ClassLoader shadowLoader;

        public void setClasspath(Path classpath) {
            if (this.classpath == null) {
                this.classpath = classpath;
            } else {
                this.classpath.append(classpath);
            }
        }

        public Path createClasspath() {
            if (this.classpath == null) {
                this.classpath = new Path(this.getProject());
            }
            return this.classpath.createPath();
        }

        public void setClasspathRef(Reference r) {
            this.createClasspath().setRefid(r);
        }

        public void setSourcepath(Path sourcepath) {
            if (this.sourcepath == null) {
                this.sourcepath = sourcepath;
            } else {
                this.sourcepath.append(sourcepath);
            }
        }

        public Path createSourcepath() {
            if (this.sourcepath == null) {
                this.sourcepath = new Path(this.getProject());
            }
            return this.sourcepath.createPath();
        }

        public void setSourcepathRef(Reference r) {
            this.createSourcepath().setRefid(r);
        }

        public void setModulepath(Path modulepath) {
            if (this.modulepath == null) {
                this.modulepath = modulepath;
            } else {
                this.modulepath.append(modulepath);
            }
        }

        public Path createModulepath() {
            if (this.modulepath == null) {
                this.modulepath = new Path(this.getProject());
            }
            return this.modulepath.createPath();
        }

        public void setModulepathRef(Reference r) {
            this.createModulepath().setRefid(r);
        }

        public void setFrom(File dir) {
            this.fromDir = dir;
        }

        public void setTo(File dir) {
            this.toDir = dir;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public void addFileset(FileSet set) {
            if (this.path == null) {
                this.path = new Path(this.getProject());
            }
            this.path.add((ResourceCollection)set);
        }

        public void addFormat(Format format) {
            this.formatOptions.add(format);
        }

        public static Class<?> shadowLoadClass(String name) {
            try {
                if (shadowLoader == null) {
                    try {
                        Class.forName("lombok.core.LombokNode");
                        shadowLoader = Delombok.class.getClassLoader();
                    } catch (ClassNotFoundException classNotFoundException) {
                        Class<?> launcherMain = Class.forName("lombok.launch.Main");
                        Method m = launcherMain.getDeclaredMethod("getShadowClassLoader", new Class[0]);
                        m.setAccessible(true);
                        shadowLoader = (ClassLoader)m.invoke(null, new Object[0]);
                    }
                }
                return Class.forName(name, true, shadowLoader);
            } catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    t = t.getCause();
                }
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }
                if (t instanceof Error) {
                    throw (Error)t;
                }
                throw new RuntimeException(t);
            }
        }

        public void execute() throws BuildException {
            Location loc = this.getLocation();
            try {
                Object instance = Delombok.shadowLoadClass("lombok.delombok.ant.DelombokTaskImpl").getConstructor(new Class[0]).newInstance(new Object[0]);
                Field[] fieldArray = ((Object)((Object)this)).getClass().getDeclaredFields();
                int n = fieldArray.length;
                int n2 = 0;
                while (n2 < n) {
                    Field selfField = fieldArray[n2];
                    if (!selfField.isSynthetic() && !Modifier.isStatic(selfField.getModifiers())) {
                        Field otherField = instance.getClass().getDeclaredField(selfField.getName());
                        otherField.setAccessible(true);
                        if (selfField.getName().equals("formatOptions")) {
                            ArrayList<String> rep = new ArrayList<String>();
                            for (Format f : this.formatOptions) {
                                if (f.getValue() == null) {
                                    throw new BuildException("'value' property required for <format>");
                                }
                                rep.add(f.getValue());
                            }
                            otherField.set(instance, rep);
                        } else {
                            otherField.set(instance, selfField.get((Object)this));
                        }
                    }
                    ++n2;
                }
                Method m = instance.getClass().getMethod("execute", Location.class);
                m.invoke(instance, loc);
            } catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    t = t.getCause();
                }
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }
                if (t instanceof Error) {
                    throw (Error)t;
                }
                throw new RuntimeException(t);
            }
        }
    }

    public static class Format {
        private String value;

        public int hashCode() {
            int result = 1;
            result = 31 * result + (this.value == null ? 0 : this.value.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            Format other = (Format)obj;
            return !(this.value == null ? other.value != null : !this.value.equals(other.value));
        }

        public String toString() {
            return "FormatOption [value=" + this.value + "]";
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

