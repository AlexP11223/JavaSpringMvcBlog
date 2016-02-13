A simple blog created during Java/OOP course in university.

Username/password auth, admin can add/edit/delete blog posts, users (after registration) can add comments, vote (+1/-1) for posts and comments.

Can be useful for someone who is learning the techonologies/frameworks used here.

The project is deployed here http://109.120.169.99:8080/blog, admin username/password — admin/pass123, the same password for other users, Alice/Bob/User1-5.

- Spring MVC, Spring Security. Spring profiles.
- Hibernate, MySQL database.
- Thymeleaf, Bootstrap, jQuery.
- [PageDown](https://code.google.com/archive/p/pagedown/) (Markdown editor), and [Txtmark](https://github.com/rjeschke/txtmark) in backend.
- Tests (unit and integration). JUnit, Mockito, DBUnit with HSQLDB in-memory database (and Spring-Test/Spring-Test-DBUnit of course, since I was using Spring).

Features:
- Posts and comments support Markdown syntax.
- Post can be split into two parts to show only the first (short) part on the main page and the full content when navigated to the post page.
- Comment hierarchy (up to 6 levels): user can reply to the specified comment instead of just adding comment at the bottom.
- Users can fill some info ("about me" text, website link) and upload avatar image, that will be displayed in their profiles (`/users/username` url or link on each username in the comments section), and small avatar image is also displayed in each comment.
- Users can change e-mail and password ("Edit settings" button).
- Admin can edit posts, hide posts (will not be visible for other users) or completely delete it from the database. (buttons at the bottom of each post, the delete button is shown only after the post is hidden)
- Users can delete their comments during 10 minutes after it was added and edit during 3 hours. Admin can edit/delete any comment.
- Search posts by tag: click on a tag under post title or via input field in the right column, it also allows multiple comma-separated tags ("java, databases" will show posts that have only both of these tags).
- Users can like (+1) and dislike (-1) posts and comments.
- List of 10 latest posts and TOP 10 posts (by user rating sum) in the right column.


#Installation

Requirements:
- JDK 8.
- Maven.
- Database, such as MySQL.
- Web server, such as Apache Tomcat 8.0 (tested only on Tomcat, probably works on other web servers too, maybe requires minor modifications).

#

1. Obtain the project source files (`git clone` or download and extract zip archive).
2. Modify configuration if needed.

  Spring profiles are used to apply different configuration files for development (**dev**), real server (**prod**) and tests (**test**).
  
  * **uploading-&lt;profile&gt;.properties** files (in `src\main\resources` directory) contain directory path for user uploaded files (such as avatar images).

    Example (**uploading-prod.properties**):
    <pre>uploading.dirpath=<b>/var/blog/upload/</b></pre>
  * **datasource-&lt;profile&gt;.xml** files (in `src\main\resources directory`) contain database configuration: database driver, address, name, username/password, ...
  
    <pre>&lt;bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close"&gt;
        &lt;property name="driverClassName" value="<b>com.mysql.jdbc.Driver</b>" /&gt;
        &lt;property name="url" value="jdbc:<b>mysql://localhost:3306/blog</b>" /&gt;
        &lt;property name="username" value="<b>BlogDbUser</b>" /&gt;
        &lt;property name="password" value="<b>BlogDbPassword</b>" /&gt;
        ...
           &lt;entry key="hibernate.dialect" value="org.hibernate.dialect.<b>MySQL5Dialect</b>"/&gt;
           ...</pre>
           
    The database must contain schema with the specified name (**blog** by default) and user with the specified name/password.
    
    If you want to use another database instead of MySQL you will need to modify `driverClassName`, `url` and `hibernate.dialect` values (highlighted above) and supply the JDBC driver (such as by adding Maven dependency to pom.xml).

    This line specifies SQL files that will be executed when database is created (by default it fills database with some demo data and creates a table for Spring Security “remember me” feature)
    
	<pre>&lt;entry key="hibernate.hbm2ddl.import_files" value="<b>/security-tables.sql,/dummy-data.sql</b>" /&gt;</pre>

    Also note this line:
    <pre>&lt;entry key="hibernate.hbm2ddl.auto" value="<b>create</b>"/&gt;</pre>
    It will drop and create database tables each time you deploy the project.

    See Hibernate, JDBC and Spring documentation for more information about possible configuration parameters.

    By default the same **datasource-dev.xml** file is used for both dev and prod profiles. If needed, you can create a separate file (datasource-prod.xml) and modify <code>&lt;beans profile="prod"&gt;</code> node in **database.xml** to use it:
    <pre>&lt;beans profile="prod"&gt;
        &lt;import resource="classpath:/datasource-prod.xml"/&gt;
	&lt;/beans&gt;</pre>

3. Run Maven `verify` goal. This will download all dependencies, run JUnit tests and build WAR file. Check Maven output to see if all tests and build are completed successfully.
4. Set `-Dspring.profiles.active` system property to specify profile that will be used. If not set the default profile is **dev**.

  For example in `<tomcat_dir>/bin/setenv.sh`:
  <pre>JAVA_OPTS="$JAVA_OPTS <b>-Dspring.profiles.active=prod</b>"</pre>
5. Deploy the WAR file to Tomcat (or other web server).
6. Go to `http://your-server-address/blog` (if deployed with default Tomcat settings) to see if it is working.
