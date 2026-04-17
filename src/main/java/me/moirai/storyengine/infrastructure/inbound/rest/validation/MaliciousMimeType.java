package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.util.Arrays;

public enum MaliciousMimeType {

    EXECUTABLE("application/x-executable"),
    DOS_EXEC("application/x-dosexec"),
    MS_DOWNLOAD("application/x-msdownload"),
    PE_FORMAT("application/vnd.microsoft.portable-executable"),
    MACH_BINARY("application/x-mach-binary"),
    SHARED_LIB("application/x-sharedlib"),
    MS_INSTALLER("application/x-ms-installer"),
    SHELL("application/x-sh"),
    SHELL_TEXT("text/x-sh"),
    SHELL_SCRIPT("text/x-shellscript"),
    BASH("application/x-bash"),
    CSH("application/x-csh"),
    TCSH("application/x-tcsh"),
    PYTHON("text/x-python"),
    PYTHON_APP("application/x-python"),
    PYTHON_BYTECODE("application/x-bytecode.python"),
    PERL("text/x-perl"),
    PERL_APP("application/x-perl"),
    RUBY("text/x-ruby"),
    RUBY_APP("application/x-ruby"),
    PHP_TEXT("text/x-php"),
    PHP_APP("application/x-php"),
    PHP("text/php"),
    POWERSHELL_APP("application/x-powershell"),
    POWERSHELL_TEXT("text/x-powershell"),
    MSDOS_BATCH("text/x-msdos-batch"),
    MSDOS_PROGRAM("application/x-msdos-program"),
    VBSCRIPT_TEXT("text/vbscript"),
    VBSCRIPT_APP("application/x-vbscript"),
    HTA("application/hta"),
    JAVA_CLASS("application/java-vm"),
    JAR("application/java-archive"),
    JAVASCRIPT_APP("application/javascript"),
    JAVASCRIPT_TEXT("text/javascript"),
    JAVASCRIPT_X("application/x-javascript"),
    HTML("text/html"),
    SVG("image/svg+xml"),
    XLSM("application/vnd.ms-excel.sheet.macroEnabled.12"),
    XLTM("application/vnd.ms-excel.template.macroEnabled.12"),
    XLAM("application/vnd.ms-excel.addin.macroEnabled.12"),
    DOCM("application/vnd.ms-word.document.macroEnabled.12"),
    DOTM("application/vnd.ms-word.template.macroEnabled.12"),
    PPTM("application/vnd.ms-powerpoint.presentation.macroEnabled.12"),
    OCTET_STREAM("application/octet-stream");

    private final String mimeType;

    MaliciousMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static boolean contains(String detectedType) {
        return Arrays.stream(values()).anyMatch(m -> m.mimeType.equals(detectedType));
    }
}
