/*
Licensed under MIT.
If you want to distribute this content, please credit me properly with a link to this repository.
*/

package best.nquantum.api.config;

import best.nquantum.ExilideClient;
import best.nquantum.api.module.Module;
import best.nquantum.api.property.Property;
import best.nquantum.api.property.impl.BooleanProperty;
import best.nquantum.api.property.impl.DoubleProperty;
import best.nquantum.api.property.impl.EnumProperty;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ConfigSystem {
    get;
    final File directory = new File(ExilideClient.get.getName());
    public final static String EXTENSION = ".exi";
    final String delimiter = ":";

    public final void saveConfig(final String fileName) throws FileNotFoundException {
        File targetFile = new File(directory + "\\" + fileName + EXTENSION);
        if(!directory.exists()) {
            directory.mkdir();
        }

        if(targetFile.exists() && targetFile.getName().equals("default.exi")){
            targetFile.delete();
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PrintWriter writer = new PrintWriter(targetFile);
        ExilideClient.get.getModManager().getModulesParsedForConfig(delimiter).forEach(m -> {
            writer.println(m);
        });
        ExilideClient.get.getModManager().getModules().forEach(m -> {
            ExilideClient.get.getModManager().getSettingsByModule(m).forEach(p -> {
                if(p instanceof DoubleProperty){
                    writer.println("PROPERTY" + delimiter + p.getName() + delimiter + ((DoubleProperty) p).getValue());
                }
                if(p instanceof EnumProperty){
                    writer.println("PROPERTY" + delimiter + p.getName() + delimiter + ((EnumProperty) p).getSelected());
                }
                if(p instanceof BooleanProperty){
                    writer.println("PROPERTY" + delimiter + p.getName() + delimiter + ((BooleanProperty) p).isEnabled());
                }
            });
        });
        writer.println("creationdate:" + System.currentTimeMillis());
        writer.flush();
        writer.close();
    }

    public final List<Module> loadConfig(final String fileName) throws IOException {
        final List<String> lines = new ArrayList<>();
        File targetFile = new File(directory + "\\" + fileName + EXTENSION);

        BufferedReader reader = new BufferedReader(new FileReader(targetFile));
        String line = reader.readLine();
        while(line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        for (final Module m : ExilideClient.get.getModManager().getModules()) {
            m.setToggled(false);
        }
        lines.forEach((s) -> {
            String[] args = s.split(delimiter);
            if(!s.contains("creationdate")) {
                switch (args[0]) {
                    case "MODULE": {
                        Module m = ExilideClient.get.getModManager().getModuleByName(args[1].toLowerCase());
                        if (m == null) break;
                        m.setKey(Integer.parseInt(args[2]));
                        m.setToggled(Boolean.parseBoolean(args[3]));
                        break;
                    }
                    case "PROPERTY": {
                        Property property = ExilideClient.get.getModManager().getPropertyByName(args[1].toLowerCase());
                        if (property instanceof EnumProperty) {
                            ((EnumProperty) property).setSelected(args[2]);
                        }
                        if (property instanceof BooleanProperty) {
                            ((BooleanProperty) property).setEnabled(Boolean.parseBoolean(args[2]));
                        }
                        if (property instanceof DoubleProperty) {
                            ((DoubleProperty) property).setValue(Double.parseDouble(args[2]));
                        }
                        break;
                    }
                }

            }
        });
        return ExilideClient.get.getModManager().getModules();
    }
    
    public final String getConfigCreationDate(String configName) throws IOException {
        final List<String> lines = new ArrayList<>();
        String dt = null;
        final List<Module> mods = new ArrayList<>();
        File targetFile = new File(directory + "\\" + configName + EXTENSION);
        final BufferedReader reader = new BufferedReader(new FileReader(targetFile));
        String line = reader.readLine();
        while(line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        for (String s : lines) {
            String[] args = s.split(delimiter);
            if(s.contains("creation")){
                final long millis = Long.parseLong(args[1]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                Date d = new Date(millis);
                dt = (sdf.format(d));
            }
        }
        return dt;
    }
}
