package net.earthcomputer.altreality.engine.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.earthcomputer.altreality.AltReality;
import net.earthcomputer.altreality.engine.Constants;
import net.earthcomputer.altreality.engine.Identifier;
import net.earthcomputer.altreality.mixin.engine.resources.class_266Accessor;
import net.earthcomputer.altreality.mixin.engine.resources.SoundHelperAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_266;
import net.minecraft.class_267;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SoundLoader {
    private static final Path SOUND_DIR = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID).resolve("sounds");
    private static Map<String, SoundEvent> soundEvents;

    public static void loadSounds() throws IOException {
        InputStream resource = AltReality.class.getResourceAsStream(Identifier.of(Constants.MOD_ID, "sounds.json").getResourcePath());
        if (resource == null) {
            throw new IllegalStateException("Unable to find sounds.json");
        }
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new SoundFile.SFTypeAdapterFactory()).create();
        soundEvents = gson.fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), new TypeToken<Map<String, SoundEvent>>(){}.getType());

        if (Files.exists(SOUND_DIR)) {
            deleteRecursive(SOUND_DIR);
        }

        Set<Identifier> alreadyExtractedSounds = new HashSet<>();
        for (SoundEvent soundEvent : soundEvents.values()) {
            for (SoundFile soundFile : soundEvent.sounds) {
                Identifier id = Identifier.parse(soundFile.name);
                if (!alreadyExtractedSounds.add(id)) {
                    continue;
                }
                String resourcePath = id.getResourcePath("sounds");
                resource = AltReality.class.getResourceAsStream(resourcePath);
                if (resource == null) {
                    System.out.println("Unable to find sound file: " + soundFile.name);
                    continue;
                }
                Path outputPath = SOUND_DIR.resolve(id.getNamespace()).resolve(id.getPath());
                Files.createDirectories(outputPath.getParent());
                Files.copy(resource, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Files.createDirectories(SOUND_DIR);
        Files.write(SOUND_DIR.resolve("READ_ME_IM_VERY_IMPORTANT.txt"), Collections.singletonList("Files in this directory will be deleted the next time the game is run"), StandardOpenOption.CREATE);
    }

    private static void deleteRecursive(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            try (Stream<Path> subFiles = Files.list(dir)) {
                for (Path subFile : (Iterable<Path>) subFiles::iterator) {
                    deleteRecursive(subFile);
                }
            }
        }
        Files.delete(dir);
    }

    public static void reloadSounds(Minecraft mc) {
        soundEvents.forEach((eventName, soundEvent) -> {
            if (soundEvent.replace) {
                SoundHelperAccessor accessor = (SoundHelperAccessor) mc.soundHelper;
                for (class_266 class_266 : new class_266[] {accessor.getField_2668(), accessor.getField_2669(), accessor.getField_2670()}) {
                    List<class_267> previousSounds = ((class_266Accessor) class_266).getField_1089().get(eventName);
                    if (previousSounds != null) {
                        class_266.field_1086 -= previousSounds.size();
                        ((class_266Accessor) class_266).getField_1090().removeIf(it -> it.field_2126.equals(eventName));
                        ((class_266Accessor) class_266).getField_1089().remove(eventName);
                    }
                }
            }

            for (SoundFile sound : soundEvent.sounds) {
                Identifier id = Identifier.parse(sound.name);
                File file = SOUND_DIR.resolve(id.getNamespace()).resolve(id.getPath()).toFile();
                if (sound.stream) {
                    mc.soundHelper.method_2016(eventName, file);
                } else if (sound.music) {
                    mc.soundHelper.method_2018(eventName, file);
                } else {
                    mc.soundHelper.method_2011(eventName, file);
                }
            }
        });
    }

    private static class SoundEvent {
        public boolean replace = false;
        public List<SoundFile> sounds = Collections.emptyList();
    }

    private static class SoundFile {
        public String name = "";
        //public float volume = 1;
        //public float pitch = 1;
        //public int weight = 1;
        public boolean stream = false;
        public boolean music = false;

        private static class SFTypeAdapterFactory implements TypeAdapterFactory {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() != SoundFile.class) {
                    return null;
                }

                TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        delegate.write(out, value);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.BEGIN_OBJECT) {
                            return delegate.read(in);
                        } else {
                            SoundFile soundFile = new SoundFile();
                            soundFile.name = in.nextString();
                            return (T) soundFile;
                        }
                    }
                };
            }
        }
    }
}
