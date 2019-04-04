package com.proxy.kiwi.utils;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public class WindowsCommandLine {
    private Kernel32 kernel32;
    private Shell32 shell32;

    /**
     * This method will try to solve issue when java executable cannot transfer
     * argument in utf encoding. cyrillic languages screws up and application
     * receives ??????? instead of real text
     */
    public String[] getCommandLineArguments(String[] fallBackTo) {
        try {
            String[] ret = getFullCommandLine();

            List<String> argsOnly = null;
            for (int i = 0; i < ret.length; i++) {
                if (argsOnly != null) {
                    argsOnly.add(ret[i]);
                } else if (ret[i].toLowerCase().endsWith(".jar")) {
                    argsOnly = new ArrayList<>();
                }
            }
            if (argsOnly != null) {
                ret = argsOnly.toArray(new String[0]);
            }

            return ret;
        } catch (Throwable t) {
            return fallBackTo;
        }
    }

    private String[] getFullCommandLine() {
        try {
            // int pid = kernel32.GetCurrentProcessId();
            IntByReference argc = new IntByReference();
            Pointer argv_ptr = getShell32().CommandLineToArgvW(getKernel32().GetCommandLineW(), argc);
            String[] argv = argv_ptr.getWideStringArray(0, argc.getValue());
            getKernel32().LocalFree(argv_ptr);
            return argv;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get program arguments using JNA", t);
        }
    }

    private Kernel32 getKernel32() {
        if (kernel32 == null) {
            kernel32 = Native.loadLibrary("kernel32", Kernel32.class);
        }
        return kernel32;
    }

    private Shell32 getShell32() {
        if (shell32 == null) {
            shell32 = Native.loadLibrary("shell32", Shell32.class);
        }
        return shell32;
    }

}

interface Kernel32 extends StdCallLibrary {
    int GetCurrentProcessId();

    WString GetCommandLineW();

    Pointer LocalFree(Pointer pointer);
}

interface Shell32 extends StdCallLibrary {
    Pointer CommandLineToArgvW(WString command_line, IntByReference argc);
}