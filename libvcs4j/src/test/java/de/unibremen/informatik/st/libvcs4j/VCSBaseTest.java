package de.unibremen.informatik.st.libvcs4j;

import de.unibremen.informatik.st.libvcs4j.exception.IllegalIntervalException;
import de.unibremen.informatik.st.libvcs4j.exception.IllegalTargetException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class defines several basic tests for {@link VCSEngine}
 * implementations. Each engine should extend this test class and implement the
 * required methods.
 */
@SuppressWarnings("Duplicates")
public abstract class VCSBaseTest {

	private Path input;

	private Path target;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		final Extractor extractor = new Extractor();
		input = extractor.extractTarGZResource(getTarGZFile());
		input = input.resolve(getFolderInTarGZ());
		// create a random path that does not exist
		target = Files.createTempDirectory(null);
		Files.delete(target);
	}

	protected Path getInput() {
		return input;
	}

	protected Path getTarget() {
		return target;
	}

	protected VCSEngineBuilder createBuilder() {
		VCSEngineBuilder builder = VCSEngineBuilder.of(input.toString());
		setEngine(builder);
		return builder;
	}

	private List<String> readIds(String idFile) throws IOException {
		InputStream is = getClass().getResourceAsStream("/" + idFile);
		String input = IOUtils.toString(is, StandardCharsets.UTF_8);
		String[] ids = input.split("\n");
		return Arrays.asList(ids);
	}

	@Test
	public void existingTargetDirectory() throws IOException {
		Path tmpDir = Files.createTempDirectory(null);
		VCSEngineBuilder builder = createBuilder();
		builder.withTarget(tmpDir);
		thrown.expect(IllegalTargetException.class);
		builder.build();
	}

	@Test
	public void existingTargetFile() throws IOException {
		Path tmpFile = Files.createTempFile(null, null);
		VCSEngineBuilder builder = createBuilder();
		builder.withTarget(tmpFile);
		thrown.expect(IllegalTargetException.class);
		builder.build();
	}

	@Test
	public void notExistingRoot() throws IOException {
		VCSEngine engine = createBuilder()
				.withRoot("yf928y298fy4f32f98fy39fy38943yf938y")
				.build();
		assertFalse(engine.next().isPresent());
	}

	@Test
	public void sinceAfterUntil() {
		VCSEngineBuilder builder = createBuilder();
		LocalDateTime dt = LocalDateTime.of(2010, 1, 1, 0, 0);
		builder.withSince(dt);
		builder.withUntil(dt.minusSeconds(1));
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void sinceEqualsUntil() {
		VCSEngineBuilder builder = createBuilder();
		LocalDateTime dt = LocalDateTime.of(2010, 1, 1, 0, 0);
		builder.withSince(dt);
		builder.withUntil(dt);
		builder.build();
	}

	@Test
	public void sinceBeforeUntil() {
		VCSEngineBuilder builder = createBuilder();
		LocalDateTime dt = LocalDateTime.of(2010, 1, 1, 0, 0);
		builder.withSince(dt);
		builder.withUntil(dt.plusSeconds(1));
		builder.build();
	}

	@Test
	public void startNegative() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(-1);
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void startZero() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(0);
		builder.build();
	}

	@Test
	public void startPositive() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(10);
		builder.build();
	}

	@Test
	public void endNegative() {
		VCSEngineBuilder builder = createBuilder();
		builder.withEnd(-1);
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void endZero() {
		VCSEngineBuilder builder = createBuilder();
		builder.withEnd(0);
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void endPositive() {
		VCSEngineBuilder builder = createBuilder();
		builder.withEnd(10);
		builder.build();
	}

	@Test
	public void startLessEnd() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(10);
		builder.withEnd(11);
		builder.build();
	}

	@Test
	public void startEqualsEnd() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(4);
		builder.withEnd(4);
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void startGreaterEnd() {
		VCSEngineBuilder builder = createBuilder();
		builder.withStart(7);
		builder.withEnd(6);
		thrown.expect(IllegalIntervalException.class);
		builder.build();
	}

	@Test
	public void processAll() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		assertTrue(commitIds.size() >= 20);
		VCSEngine engine = createBuilder().build();
		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(commitIds.size(), versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void processSubDir() throws IOException {
		List<String> commitIds = readIds(getSubDirCommitIdFile());
		List<String> revisionIds = readIds(getSubDirRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		assertFalse(commitIds.isEmpty());
		assertTrue(commitIds.size() < readIds(getRootCommitIdFile()).size());
		VCSEngine engine = createBuilder().withRoot(getSubDir()).build();
		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(commitIds.size(), versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void rangeInterval0To3() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		VCSEngine engine = createBuilder()
				.withStart(0)
				.withEnd(3)
				.build();

		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(3, versions.size());
		for (int i = 3; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void rangeInterval5To9() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		VCSEngine engine = createBuilder()
				.withStart(5)
				.withEnd(9)
				.build();

		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(4, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i + 5), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i + 5), v.getRevision().getId());
		}
	}

	@Test
	public void rangeIntervalTo2() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		VCSEngine engine = createBuilder()
				.withEnd(2)
				.build();

		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(2, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void rangeIntervalLast3() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		int start = commitIds.size() - 3;
		VCSEngine engine = createBuilder()
				.withStart(start)
				.build();

		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(3, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i + start),
					v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i + start),
					v.getRevision().getId());
		}
	}

	@Test
	public void revisionIntervalIdx0To5() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		String from = commitIds.get(0);
		String to = commitIds.get(5);
		VCSEngine engine = createBuilder()
				.withFrom(from)
				.withTo(to)
				.build();
		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(6, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void revisionIntervalIdx6To8() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		String from = commitIds.get(6);
		String to = commitIds.get(8);
		VCSEngine engine = createBuilder()
				.withFrom(from)
				.withTo(to)
				.build();
		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(3, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(commitIds.get(i + 6), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i + 6), v.getRevision().getId());
		}
	}

	@Test
	public void revisionIntervalIdxTo3() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		String to = commitIds.get(3);
		VCSEngine engine = createBuilder()
				.withTo(to)
				.build();
		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(4, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(commitIds.get(i), v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i), v.getRevision().getId());
		}
	}

	@Test
	public void revisionIntervalLast4() throws IOException {
		List<String> commitIds = readIds(getRootCommitIdFile());
		List<String> revisionIds = readIds(getRootRevisionIdFile());
		assertEquals(commitIds.size(), revisionIds.size());
		int start = commitIds.size() - 4;
		VCSEngine engine = createBuilder()
				.withFrom(commitIds.get(start))
				.build();

		List<Version> versions = new ArrayList<>();
		engine.forEach(versions::add);
		assertEquals(4, versions.size());
		for (int i = 0; i < versions.size(); i++) {
			Version v = versions.get(i);
			assertEquals(i + 1, v.getOrdinal());
			assertEquals(commitIds.get(i + start),
					v.getLatestCommit().getId());
			assertEquals(revisionIds.get(i + start),
					v.getRevision().getId());
		}
	}

	/**
	 * Returns the path of the archive to extract, i.e. 'javacpp.tar.gz'.
	 *
	 * @return
	 * 		The path of the archive to extract.
	 */
	protected abstract String getTarGZFile();

	/**
	 * Returns the path within {@link #getTarGZFile()} containing the VCS.
	 *
	 * @return
	 * 		The path within {@link #getTarGZFile()} containing the VCS.
	 */
	protected abstract String getFolderInTarGZ();

	/**
	 * Returns the path of the file containing the commit ids of the root
	 * directory of the VCS to process, i.e. 'javacpp_ids.txt'. It is assumed
	 * that the file is UTF-8 encoded, contains at least 20 ids, and stores the
	 * ids in ascending order.
	 *
	 * @return
	 * 		The path of the file containing the commit ids of the root
	 * 		directory of the VCS to process.
	 */
	protected abstract String getRootCommitIdFile();

	/**
	 * Same as {@link #getRootCommitIdFile()}, but for revision ids. The
	 * default implementation returns {@link #getRootCommitIdFile()} as most
	 * VCS do not differ between commit and revision ids.
	 *
	 * @return
	 * 		Same as {@link #getRootCommitIdFile()}, but for revision ids.
	 */
	protected String getRootRevisionIdFile() {
		return getRootCommitIdFile();
	}

	/**
	 * Returns the path of the subdirectory corresponding to
	 * {@link #getSubDirCommitIdFile()}. The path will be passed to
	 * {@link VCSEngineBuilder#withRoot(String)}.
	 *
	 * @return
	 * 		The path of the subdirectory corresponding to
	 * 		{@link #getSubDirCommitIdFile()}.
	 */
	protected abstract String getSubDir();

	/**
	 * Returns the path of the file containing the commit ids of an arbitrary
	 * subdirectory of the VCS to process, i.e. 'javacpp_subdir_ids.txt'. It is
	 * assumed that the file is UTF-8 encoded, contains less ids than the file
	 * returned by {@link #getRootCommitIdFile()} (but still more than 0), and
	 * stores the ids in ascending order.
	 *
	 * @return
	 * 		The path of the file containing the commit ids of an arbitrary
	 * 		subdirectory of the VCS to process.
	 */
	protected abstract String getSubDirCommitIdFile();

	/**
	 * Same as {@link #getSubDirCommitIdFile()}, but for revision ids. The
	 * default implementation returns {@link #getSubDirCommitIdFile()} as most
	 * VCS do not differ between commit and revision ids.
	 *
	 * @return
	 * 		Same as {@link #getSubDirCommitIdFile()}, but for revision ids.
	 */
	protected String getSubDirRevisionIdFile() {
		return getSubDirCommitIdFile();
	}

	/**
	 * Sets the engine used to process the VCS.
	 *
	 * @param builder
	 * 		The builder whose engine is set.
	 */
	protected abstract void setEngine(VCSEngineBuilder builder);
}