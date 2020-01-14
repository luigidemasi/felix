package org.apache.felix.fileinstall.internal;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.apache.felix.fileinstall.internal.Scanner.SUBDIR_MODE_RECURSE;
import static org.apache.felix.fileinstall.internal.Scanner.SUBDIR_MODE_SKIP;

import static org.junit.Assert.assertTrue;


public class ScannerSubDirTest {

  @Rule
  public  TemporaryFolder folder = new TemporaryFolder();

  private final static String FILTER = ".*\\.(cfg|config)";
  private final static Set<File> expectedRecurseModeFiles = new HashSet<>();
  private final static Set<File> excludedRecurseModeFiles = new HashSet<>();
  private final static Set<File> expectedSkipModeFiles = new HashSet<>();
  private final static Set<File> excludedSkipModeFiles = new HashSet<>();



  @Test
  public void testScannerSubdir() throws Exception{
    prepareFiles();
    File watchedDirectory = folder.getRoot();

 // Testing the scanner with recurse mode
    Scanner recurseScanner = new Scanner(watchedDirectory, FILTER, SUBDIR_MODE_RECURSE);
    Set<File> filteredRecurseFiles = recurseScanner.scan(true);
    assertTrue(filteredRecurseFiles.size() == expectedRecurseModeFiles.size());
    assertTrue(filteredRecurseFiles.containsAll(expectedRecurseModeFiles));
    for(File file : excludedRecurseModeFiles){
      Assert.assertFalse(filteredRecurseFiles.contains(file));
    }


    // Testing the scanner with skip subdir mode
    Scanner skipScanner = new Scanner(watchedDirectory, FILTER, SUBDIR_MODE_SKIP);
    Set<File> filteredSkipFiles = skipScanner.scan(true);

    //Assertions

    assertTrue(filteredSkipFiles.size() == expectedSkipModeFiles.size());
    assertTrue(filteredSkipFiles.containsAll(expectedSkipModeFiles));
    for(File file : excludedSkipModeFiles){
      Assert.assertFalse(filteredSkipFiles.contains(file));
    }
  }


  private void prepareFiles() throws IOException {
    File cfgFirst = folder.newFile("first.cfg");
    File mdFirst = folder.newFile("first.md");

    excludedRecurseModeFiles.add(mdFirst);
    expectedRecurseModeFiles.add(cfgFirst);
    excludedSkipModeFiles.add(mdFirst);
    expectedSkipModeFiles.add(cfgFirst);

    // first level subfolder and files
    //expected
    File firstLevelSubfolder = folder.newFolder("firstSubfolder");
    Path firstLevelSubfolderCfgFilePath = Paths.get(firstLevelSubfolder.getPath()+File.separator+"second.cfg");
    File firstLevelSubfolderCfgFile = new File(firstLevelSubfolderCfgFilePath.toString());
    firstLevelSubfolderCfgFile.createNewFile();
    expectedRecurseModeFiles.add(firstLevelSubfolderCfgFile);
    excludedSkipModeFiles.add(firstLevelSubfolderCfgFile);
    //md
    Path firstLevelSubfolderMdFilePath = Paths.get(firstLevelSubfolder.getPath()+File.separator+"second.md");
    File firstLevelSubfolderMdFile = new File(firstLevelSubfolderMdFilePath.toString());
    firstLevelSubfolderMdFile.createNewFile();
    excludedRecurseModeFiles.add(firstLevelSubfolderMdFile);
    excludedSkipModeFiles.add(firstLevelSubfolderMdFile);

    // second level subfolder and files
    //cfg
    Path secondLevelSubfolderCfgFilePath = Paths.get(firstLevelSubfolder.getPath() + File.separator+"secondSubfolder" + File.separator + "third.config");
    File secondLevelSubfolderCfgFile = new File(secondLevelSubfolderCfgFilePath.toString());
    secondLevelSubfolderCfgFile.getParentFile().mkdirs();
    secondLevelSubfolderCfgFile.createNewFile();
    expectedRecurseModeFiles.add(secondLevelSubfolderCfgFile);
    excludedSkipModeFiles.add(secondLevelSubfolderCfgFile);
    //Txt
    Path secondLevelSubfolderTxtFilePath = Paths.get(firstLevelSubfolder.getPath() + File.separator+"secondSubfolder" + File.separator + "third.txt");
    File secondLevelSubfolderTxtFile = new File(secondLevelSubfolderTxtFilePath.toString());
    secondLevelSubfolderTxtFile.createNewFile();
    excludedRecurseModeFiles.add(secondLevelSubfolderTxtFile);
    excludedSkipModeFiles.add(secondLevelSubfolderTxtFile);

  }

}
