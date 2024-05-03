/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class ExceptionUtils {
    static final String WRAPPED_MARKER = " [wrapped] ";
    private static final String[] CAUSE_METHOD_NAMES = new String[]{"getCause", "getNextException", "getTargetException", "getException", "getSourceException", "getRootCause", "getCausedByException", "getNested", "getLinkedException", "getNestedException", "getLinkedCause", "getThrowable"};

    @Deprecated
    public static String[] getDefaultCauseMethodNames() {
        return ArrayUtils.clone(CAUSE_METHOD_NAMES);
    }

    @Deprecated
    public static Throwable getCause(Throwable throwable) {
        return ExceptionUtils.getCause(throwable, CAUSE_METHOD_NAMES);
    }

    @Deprecated
    public static Throwable getCause(Throwable throwable, String[] methodNames) {
        if (throwable == null) {
            return null;
        }
        if (methodNames == null) {
            methodNames = CAUSE_METHOD_NAMES;
        }
        for (String methodName : methodNames) {
            Throwable cause;
            if (methodName == null || (cause = ExceptionUtils.getCauseUsingMethodName(throwable, methodName)) == null) continue;
            return cause;
        }
        return null;
    }

    public static Throwable getRootCause(Throwable throwable) {
        List<Throwable> list = ExceptionUtils.getThrowableList(throwable);
        return list.size() < 2 ? null : list.get(list.size() - 1);
    }

    private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
        Method method = null;
        try {
            method = throwable.getClass().getMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException ignored) {
        } catch (SecurityException ignored) {
            // empty catch block
        }
        if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Throwable)method.invoke(throwable, new Object[0]);
            } catch (IllegalAccessException ignored) {
            } catch (IllegalArgumentException ignored) {
            } catch (InvocationTargetException invocationTargetException) {
                // empty catch block
            }
        }
        return null;
    }

    public static int getThrowableCount(Throwable throwable) {
        return ExceptionUtils.getThrowableList(throwable).size();
    }

    public static Throwable[] getThrowables(Throwable throwable) {
        List<Throwable> list = ExceptionUtils.getThrowableList(throwable);
        return list.toArray(new Throwable[list.size()]);
    }

    public static List<Throwable> getThrowableList(Throwable throwable) {
        ArrayList<Throwable> list = new ArrayList<Throwable>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = ExceptionUtils.getCause(throwable);
        }
        return list;
    }

    public static int indexOfThrowable(Throwable throwable, Class<?> clazz) {
        return ExceptionUtils.indexOf(throwable, clazz, 0, false);
    }

    public static int indexOfThrowable(Throwable throwable, Class<?> clazz, int fromIndex) {
        return ExceptionUtils.indexOf(throwable, clazz, fromIndex, false);
    }

    public static int indexOfType(Throwable throwable, Class<?> type) {
        return ExceptionUtils.indexOf(throwable, type, 0, true);
    }

    public static int indexOfType(Throwable throwable, Class<?> type, int fromIndex) {
        return ExceptionUtils.indexOf(throwable, type, fromIndex, true);
    }

    private static int indexOf(Throwable throwable, Class<?> type, int fromIndex, boolean subclass) {
        Throwable[] throwables;
        if (throwable == null || type == null) {
            return -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (fromIndex >= (throwables = ExceptionUtils.getThrowables(throwable)).length) {
            return -1;
        }
        if (subclass) {
            for (int i = fromIndex; i < throwables.length; ++i) {
                if (!type.isAssignableFrom(throwables[i].getClass())) continue;
                return i;
            }
        } else {
            for (int i = fromIndex; i < throwables.length; ++i) {
                if (!type.equals(throwables[i].getClass())) continue;
                return i;
            }
        }
        return -1;
    }

    public static void printRootCauseStackTrace(Throwable throwable) {
        ExceptionUtils.printRootCauseStackTrace(throwable, System.err);
    }

    public static void printRootCauseStackTrace(Throwable throwable, PrintStream stream) {
        String[] trace;
        if (throwable == null) {
            return;
        }
        if (stream == null) {
            throw new IllegalArgumentException("The PrintStream must not be null");
        }
        for (String element : trace = ExceptionUtils.getRootCauseStackTrace(throwable)) {
            stream.println(element);
        }
        stream.flush();
    }

    public static void printRootCauseStackTrace(Throwable throwable, PrintWriter writer) {
        String[] trace;
        if (throwable == null) {
            return;
        }
        if (writer == null) {
            throw new IllegalArgumentException("The PrintWriter must not be null");
        }
        for (String element : trace = ExceptionUtils.getRootCauseStackTrace(throwable)) {
            writer.println(element);
        }
        writer.flush();
    }

    public static String[] getRootCauseStackTrace(Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        int count = throwables.length;
        ArrayList<String> frames = new ArrayList<String>();
        List<String> nextTrace = ExceptionUtils.getStackFrameList(throwables[count - 1]);
        int i = count;
        while (--i >= 0) {
            List<String> trace = nextTrace;
            if (i != 0) {
                nextTrace = ExceptionUtils.getStackFrameList(throwables[i - 1]);
                ExceptionUtils.removeCommonFrames(trace, nextTrace);
            }
            if (i == count - 1) {
                frames.add(throwables[i].toString());
            } else {
                frames.add(WRAPPED_MARKER + throwables[i].toString());
            }
            for (int j = 0; j < trace.size(); ++j) {
                frames.add(trace.get(j));
            }
        }
        return frames.toArray(new String[frames.size()]);
    }

    public static void removeCommonFrames(List<String> causeFrames, List<String> wrapperFrames) {
        if (causeFrames == null || wrapperFrames == null) {
            throw new IllegalArgumentException("The List must not be null");
        }
        int causeFrameIndex = causeFrames.size() - 1;
        for (int wrapperFrameIndex = wrapperFrames.size() - 1; causeFrameIndex >= 0 && wrapperFrameIndex >= 0; --causeFrameIndex, --wrapperFrameIndex) {
            String wrapperFrame;
            String causeFrame = causeFrames.get(causeFrameIndex);
            if (!causeFrame.equals(wrapperFrame = wrapperFrames.get(wrapperFrameIndex))) continue;
            causeFrames.remove(causeFrameIndex);
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static String[] getStackFrames(Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return ExceptionUtils.getStackFrames(ExceptionUtils.getStackTrace(throwable));
    }

    static String[] getStackFrames(String stackTrace) {
        String linebreak = SystemUtils.LINE_SEPARATOR;
        StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        ArrayList<String> list = new ArrayList<String>();
        while (frames.hasMoreTokens()) {
            list.add(frames.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    static List<String> getStackFrameList(Throwable t) {
        String stackTrace = ExceptionUtils.getStackTrace(t);
        String linebreak = SystemUtils.LINE_SEPARATOR;
        StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
        ArrayList<String> list = new ArrayList<String>();
        boolean traceStarted = false;
        while (frames.hasMoreTokens()) {
            String token = frames.nextToken();
            int at = token.indexOf("at");
            if (at != -1 && token.substring(0, at).trim().isEmpty()) {
                traceStarted = true;
                list.add(token);
                continue;
            }
            if (!traceStarted) continue;
            break;
        }
        return list;
    }

    public static String getMessage(Throwable th) {
        if (th == null) {
            return "";
        }
        String clsName = ClassUtils.getShortClassName(th, null);
        String msg = th.getMessage();
        return clsName + ": " + StringUtils.defaultString(msg);
    }

    public static String getRootCauseMessage(Throwable th) {
        Throwable root = ExceptionUtils.getRootCause(th);
        root = root == null ? th : root;
        return ExceptionUtils.getMessage(root);
    }
}

