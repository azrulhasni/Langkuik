/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom.attachment;

import com.fasterxml.uuid.Generators;
import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.receivers.FileData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public class LangkuikMultiFileBuffer implements MultiFileReceiver {

    private Map<String, FileData> files = new HashMap<>();

    private Map<String, String> tempFileNames = new HashMap<>();

    private Optional<FileOutputStream> createFileOutputStream(String fileName) {

        try {
            return Optional.of(new FileOutputStream(createFile(fileName)));
        } catch (IOException ex) {
            Logger.getLogger(LangkuikMultiFileBuffer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    private File createFile(String fileName) throws IOException {
        String tempFileName = "upload_tmpfile_" + fileName + "_"
                + Generators.timeBasedGenerator().generate().toString();

        File tempFile = File.createTempFile(tempFileName, null);
        tempFileNames.put(fileName, tempFile.getPath());

        return tempFile;
    }

    @Override
    public OutputStream receiveUpload(String fileName, String MIMEType) {
        
        Optional<FileOutputStream> outputBuffer = createFileOutputStream(fileName);
        outputBuffer.ifPresent((FileOutputStream fos)->{
            files.put(fileName, new FileData(fileName, MIMEType, fos));
        });
        return outputBuffer.get();
    }

    public Set<String> getFiles() {
        return files.keySet();
    }

    public FileData getFileData(String fileName) {
        return files.get(fileName);
    }

    public InputStream getInputStream(String fileName) {
        if (tempFileNames.containsKey(fileName)) {
            try {
                //return new FileInputStream(tempFileNames.get(fileName));
                Path path = Paths.get(tempFileNames.get(fileName));
                return Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE); //Enforce files to be deleted once read
            } catch (IOException ex) {
                Logger.getLogger(LangkuikMultiFileBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return new ByteArrayInputStream(new byte[0]);
    }

}
