package me.coley.recaf.workspace.resource.source;

import me.coley.recaf.io.BiResourceConsumer;
import me.coley.recaf.util.IOUtil;
import software.coley.llzip.ZipArchive;
import software.coley.llzip.ZipCompressions;
import software.coley.llzip.ZipIO;
import software.coley.llzip.part.CentralDirectoryFileHeader;
import software.coley.llzip.part.LocalFileHeader;
import software.coley.llzip.util.ByteDataUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.zip.ZipOutputStream;

/**
 * Origin location information of archive files.
 *
 * @author Matt Coley
 */
public abstract class ArchiveFileContentSource extends ContainerContentSource<LocalFileHeader> {
	private static final int BUFFER_SIZE = (int) Math.pow(2, 20);

	protected ArchiveFileContentSource(SourceType type, Path path) {
		super(type, path);
	}

	@Override
	protected void writeContent(Path output, SortedMap<String, byte[]> content) throws IOException {
		OutputStream fos = new BufferedOutputStream(Files.newOutputStream(output), BUFFER_SIZE);
		try (ZipOutputStream zos = new ZipOutputStream(fos)) {
			Set<String> dirsVisited = new HashSet<>();
			// Contents are in sorted order, so we can insert directory entries before file entries occur.
			for (Map.Entry<String, byte[]> entry : content.entrySet()) {
				String key = entry.getKey();
				byte[] out = entry.getValue();
				// Write directories for upcoming entries if necessary
				// - Ugly, but does the job.
				if (key.contains("/")) {
					// Record directories
					String parent = key;
					List<String> toAdd = new ArrayList<>();
					do {
						parent = parent.substring(0, parent.lastIndexOf('/'));
						if (dirsVisited.add(parent)) {
							toAdd.add(0, parent + '/');
						} else break;
					} while (parent.contains("/"));
					// Put directories in order of depth
					for (String dir : toAdd) {
						zos.putNextEntry(new JarEntry(dir));
						zos.closeEntry();
					}
				}
				// Write entry content
				zos.putNextEntry(new JarEntry(key));
				zos.write(out);
				zos.closeEntry();
			}
		}
		fos.flush();
		fos.close();
	}

	@Override
	protected void consumeEach(BiResourceConsumer<LocalFileHeader> entryHandler) throws IOException {
		Path path = getPath();
		boolean delete = false;
		if (!IOUtil.isOnDefaultFileSystem(path)) {
			Files.copy(path, path = Files.createTempFile("recaf", ".jar"));
			delete = true;
		}
		try {
			handle(path, entryHandler);
		} finally {
			if (delete)
				IOUtil.deleteQuietly(path);
		}
	}

	@Override
	protected boolean isClass(LocalFileHeader entry, byte[] content) {
		// If the entry name does not have the "CAFEBABE" magic header, its not a class.
		return matchesClassMagic(content);
	}

	@Override
	protected String getPathName(LocalFileHeader entry) {
		return entry.getFileNameAsString();
	}

	@Override
	protected Predicate<LocalFileHeader> createDefaultFilter() {
		return entry -> {
			String name = entry.getFileNameAsString();
			// If the entry is a directory, then skip it....
			// Unless its a "fake" directory because archive manipulation by obfuscation
			boolean hasClassExt = name.endsWith(".class") || name.endsWith(".class/");
			if (name.endsWith("/") && !hasClassExt) {
				return false;
			}
			// Skip relative path names / directory escaping
			if (name.contains("../")) {
				return false;
			}
			// Skip if path contains zero-width sub-directory name.
			return !name.contains("//");
		};
	}

	private void handle(Path path, BiResourceConsumer<LocalFileHeader> entryHandler) throws IOException {
		Predicate<LocalFileHeader> filter = getEntryFilter();
		ZipArchive archive = ZipIO.readJvm(path);
		for (LocalFileHeader fileHeader : archive.getLocalFiles()) {
			CentralDirectoryFileHeader linked = fileHeader.getLinkedDirectoryFileHeader();
			if (linked == null)
				continue;
 			if (filter.test(fileHeader)) {
				entryHandler.accept(fileHeader, () -> ByteDataUtil.toByteArray(ZipCompressions.decompress(fileHeader)));
			}
		}
	}
}
