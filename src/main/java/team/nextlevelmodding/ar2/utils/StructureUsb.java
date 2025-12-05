package team.nextlevelmodding.ar2.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import team.nextlevelmodding.ar2.items.UsbItem;

import javax.annotation.Nullable;
import java.util.*;

public class StructureUsb {
    private static final String FILES_TAG = "Files";
    private static final String NAME_TAG = "name";
    private static final String TYPE_TAG = "type";
    private static final String CONTENTS_TAG = "contents";
    private static final String DATA_TAG = "data";
    
    // File type constants
    public static final String TYPE_FILE = "file";
    public static final String TYPE_FOLDER = "folder";
    
    /**
     * Represents a file or folder in the USB filesystem
     */
    public static class FileEntry {
        private final String name;
        private final String type;
        private String data;
        private final List<FileEntry> contents;
        
        public FileEntry(String name, String type) {
            this.name = name;
            this.type = type;
            this.contents = new ArrayList<>();
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        @Nullable
        public String getData() {
            return data;
        }
        
        public void setData(String data) {
            this.data = data;
        }
        
        public List<FileEntry> getContents() {
            return contents;
        }
        
        public void addFile(FileEntry entry) {
            if (TYPE_FOLDER.equals(type)) {
                contents.add(entry);
            }
        }
        
        @Nullable
        public FileEntry findEntry(String path) {
            if (path == null || path.isEmpty()) {
                return null;
            }
            
            String[] parts = path.split("/", 2);
            String current = parts[0];
            
            for (FileEntry entry : contents) {
                if (entry.getName().equals(current)) {
                    if (parts.length == 1) {
                        return entry;
                    } else if (TYPE_FOLDER.equals(entry.getType())) {
                        return entry.findEntry(parts[1]);
                    }
                    return null;
                }
            }
            return null;
        }
        
        public static FileEntry fromNBT(CompoundTag tag) {
            String name = tag.getString(NAME_TAG);
            String type = tag.getString(TYPE_TAG);
            FileEntry entry = new FileEntry(name, type);
            
            if (TYPE_FILE.equals(type) && tag.contains(DATA_TAG, Tag.TAG_STRING)) {
                entry.setData(tag.getString(DATA_TAG));
            } else if (TYPE_FOLDER.equals(type) && tag.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
                ListTag contents = tag.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                for (Tag item : contents) {
                    if (item instanceof CompoundTag) {
                        entry.addFile(fromNBT((CompoundTag) item));
                    }
                }
            }
            
            return entry;
        }
        
        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString(NAME_TAG, name);
            tag.putString(TYPE_TAG, type);
            
            if (TYPE_FILE.equals(type) && data != null) {
                tag.putString(DATA_TAG, data);
            } else if (TYPE_FOLDER.equals(type) && !contents.isEmpty()) {
                ListTag contentsList = new ListTag();
                for (FileEntry entry : contents) {
                    contentsList.add(entry.toNBT());
                }
                tag.put(CONTENTS_TAG, contentsList);
            }
            
            return tag;
        }
    }
    
    /**
     * Saves a file or folder to the USB drive
     * @param usb The USB item stack
     * @param path The path where to save the file (e.g., "Documents/notes.txt")
     * @param content The content to save (null for folders)
     * @return true if successful, false otherwise
     */
    public static boolean save(ItemStack usb, String path, @Nullable String content) {
        if (!(usb.getItem() instanceof UsbItem)) {
            return false;
        }
        
        String[] parts = path.split("/");
        if (parts.length == 0) {
            return false;
        }
        
        String fileName = parts[parts.length - 1];
        String parentPath = path.substring(0, Math.max(0, path.length() - fileName.length() - 1));
        
        // Get or create root files list
        ListTag filesList = getOrCreateFilesList(usb);
        
        // Handle root level files
        if (parentPath.isEmpty()) {
            // Check if file already exists
            for (int i = 0; i < filesList.size(); i++) {
                CompoundTag fileTag = filesList.getCompound(i);
                if (fileTag.getString(NAME_TAG).equals(fileName)) {
                    // Update existing file
                    if (content != null) {
                        fileTag.putString(DATA_TAG, content);
                        fileTag.putString(TYPE_TAG, TYPE_FILE);
                    }
                    return true;
                }
            }
            
            // Create new file
            CompoundTag newFile = new CompoundTag();
            newFile.putString(NAME_TAG, fileName);
            newFile.putString(TYPE_TAG, content != null ? TYPE_FILE : TYPE_FOLDER);
            if (content != null) {
                newFile.putString(DATA_TAG, content);
            }
            filesList.add(newFile);
            return true;
        }
        
        // Handle nested files
        String[] parentParts = parentPath.split("/");
        CompoundTag current = findOrCreateFolder(usb, parentParts, 0, filesList);
        
        if (current == null) {
            return false; // Couldn't create parent directories
        }
        
        // Add the file to the parent folder
        ListTag contents = current.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
        
        // Check if file already exists
        for (int i = 0; i < contents.size(); i++) {
            CompoundTag fileTag = contents.getCompound(i);
            if (fileTag.getString(NAME_TAG).equals(fileName)) {
                // Update existing file
                if (content != null) {
                    fileTag.putString(DATA_TAG, content);
                    fileTag.putString(TYPE_TAG, TYPE_FILE);
                }
                return true;
            }
        }
        
        // Create new file
        CompoundTag newFile = new CompoundTag();
        newFile.putString(NAME_TAG, fileName);
        newFile.putString(TYPE_TAG, content != null ? TYPE_FILE : TYPE_FOLDER);
        if (content != null) {
            newFile.putString(DATA_TAG, content);
        }
        contents.add(newFile);
        
        return true;
    }
    
    /**
     * Loads a file from the USB drive
     * @param usb The USB item stack
     * @param path The path of the file to load
     * @return The file content, or null if not found
     */
    @Nullable
    public static String load(ItemStack usb, String path) {
        if (!(usb.getItem() instanceof UsbItem) || !usb.hasTag()) {
            return null;
        }
        
        CompoundTag tag = usb.getTag();
        if (tag == null || !tag.contains(FILES_TAG, Tag.TAG_LIST)) {
            return null;
        }
        
        ListTag filesList = tag.getList(FILES_TAG, Tag.TAG_COMPOUND);
        String[] parts = path.split("/");
        
        if (parts.length == 1) {
            // Root level file
            for (Tag item : filesList) {
                if (item instanceof CompoundTag) {
                    CompoundTag fileTag = (CompoundTag) item;
                    if (fileTag.getString(NAME_TAG).equals(parts[0]) && 
                        fileTag.contains(DATA_TAG, Tag.TAG_STRING)) {
                        return fileTag.getString(DATA_TAG);
                    }
                }
            }
        } else {
            // Nested file
            CompoundTag current = findFolder(parts, 0, filesList);
            if (current != null) {
                String fileName = parts[parts.length - 1];
                ListTag contents = current.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                for (Tag item : contents) {
                    if (item instanceof CompoundTag) {
                        CompoundTag fileTag = (CompoundTag) item;
                        if (fileTag.getString(NAME_TAG).equals(fileName) && 
                            fileTag.contains(DATA_TAG, Tag.TAG_STRING)) {
                            return fileTag.getString(DATA_TAG);
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lists files and folders in a directory
     * @param usb The USB item stack
     * @param path The directory path (empty string for root)
     * @return List of file/folder names, or null if path doesn't exist
     */
    @Nullable
    public static List<String> list(ItemStack usb, String path) {
        if (!(usb.getItem() instanceof UsbItem) || !usb.hasTag()) {
            return null;
        }
        
        CompoundTag tag = usb.getTag();
        if (tag == null || !tag.contains(FILES_TAG, Tag.TAG_LIST)) {
            return path.isEmpty() ? new ArrayList<>() : null;
        }
        
        ListTag filesList = tag.getList(FILES_TAG, Tag.TAG_COMPOUND);
        
        if (path.isEmpty()) {
            // List root directory
            List<String> result = new ArrayList<>();
            for (Tag item : filesList) {
                if (item instanceof CompoundTag) {
                    CompoundTag fileTag = (CompoundTag) item;
                    result.add(fileTag.getString(NAME_TAG) + 
                             (TYPE_FOLDER.equals(fileTag.getString(TYPE_TAG)) ? "/" : ""));
                }
            }
            return result;
        } else {
            // List subdirectory
            String[] parts = path.split("/");
            CompoundTag current = findFolder(parts, 0, filesList);
            
            if (current != null && current.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
                ListTag contents = current.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                List<String> result = new ArrayList<>();
                
                for (Tag item : contents) {
                    if (item instanceof CompoundTag) {
                        CompoundTag fileTag = (CompoundTag) item;
                        result.add(fileTag.getString(NAME_TAG) + 
                                 (TYPE_FOLDER.equals(fileTag.getString(TYPE_TAG)) ? "/" : ""));
                    }
                }
                
                return result;
            }
        }
        
        return null;
    }
    
    // Helper method to get or create the root files list
    private static ListTag getOrCreateFilesList(ItemStack usb) {
        CompoundTag tag = usb.getOrCreateTag();
        if (!tag.contains(FILES_TAG, Tag.TAG_LIST)) {
            tag.put(FILES_TAG, new ListTag());
        }
        return tag.getList(FILES_TAG, Tag.TAG_COMPOUND);
    }
    
    // Helper method to find or create a folder structure
    @Nullable
    private static CompoundTag findOrCreateFolder(ItemStack usb, String[] path, int index, ListTag currentLevel) {
        if (index >= path.length) {
            return null;
        }
        
        String folderName = path[index];
        
        // Try to find the folder
        for (int i = 0; i < currentLevel.size(); i++) {
            CompoundTag item = currentLevel.getCompound(i);
            if (item.getString(NAME_TAG).equals(folderName)) {
                if (index == path.length - 1) {
                    return item; // Found the target folder
                } else {
                    // Continue searching in this folder
                    ListTag contents = item.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                    return findOrCreateFolder(usb, path, index + 1, contents);
                }
            }
        }
        
        // Folder doesn't exist, create it
        if (index < path.length) {
            CompoundTag newFolder = new CompoundTag();
            newFolder.putString(NAME_TAG, folderName);
            newFolder.putString(TYPE_TAG, TYPE_FOLDER);
            newFolder.put(CONTENTS_TAG, new ListTag());
            
            currentLevel.add(newFolder);
            
            if (index == path.length - 1) {
                return newFolder;
            } else {
                ListTag contents = newFolder.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                return findOrCreateFolder(usb, path, index + 1, contents);
            }
        }
        
        return null;
    }
    
    // Helper method to find a folder (without creating)
    @Nullable
    private static CompoundTag findFolder(String[] path, int index, ListTag currentLevel) {
        if (index >= path.length) {
            return null;
        }
        
        String folderName = path[index];
        
        for (int i = 0; i < currentLevel.size(); i++) {
            CompoundTag item = currentLevel.getCompound(i);
            if (item.getString(NAME_TAG).equals(folderName)) {
                if (index == path.length - 1) {
                    return item; // Found the target folder
                } else if (item.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
                    // Continue searching in this folder
                    ListTag contents = item.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
                    return findFolder(path, index + 1, contents);
                } else {
                    return null; // Not a folder or doesn't have contents
                }
            }
        }
        
        return null; // Folder not found
    }
}
