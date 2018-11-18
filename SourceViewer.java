import java.io.*;
import java.net.*;

/**
 *
 * The Source Viwer attempst to open an http conection with a provided url, searching the returned body conent for a given search string,
 * printing out each occurance if found. The source viewer attempts to set the encoding of the stream reader to a provided char set from the
 * http header, if provdied, otherwise assumes UTF-8
 *
 * @author Tyler Matthews
 */
public class SourceViewer {
   /**
    * Constant for utf encoding setting
    */
   private static String     UTF8 = "UTF-8";
   /**
    * The raw string url to open an http connection to
    */
   private String            rawURL;
   /**
    * The seach string to search for within the returned body content of http request
    */
   private String            searchString;
   /**
    * The httpconnection to the provied raw url
    */
   private HttpURLConnection httpConnection;

   /**
    * Instantiates a source viwer, checking search string validity.
    *
    * @param rawURL The url that will have an http connection open with
    * @param searchString The seach string to search for within the returned body content of http request
    * @throws Exception On invalid search string
    */
   public SourceViewer(String rawURL, String searchString) throws Exception {
      // check search string validty
      if (searchString == null)
         throw new Exception("Invalid Search string");

      this.rawURL = rawURL;
      this.searchString = searchString;
   }

   /**
    * Opens an http connection on the viewers raw url
    *
    * @throws MalformedURLException - if no protocol is specified, or an unknown protocol is found, or spec is null when creating a URL
    * @throws IOException - if an I/O exception occurs during open connection
    */
   public void openConnection() throws MalformedURLException, IOException {
      httpConnection = (HttpURLConnection) (new URL(rawURL)).openConnection();
   }

   /**
    * Opens the source viwers http connection if not open, sends http request, and searches body return with provided search string.
    *
    * @throws MalformedURLException - if no protocol is specified, or an unknown protocol is found, or spec is null when creating a URL
    * @throws IOException - if an I/O exception occurs during open connection
    */
   public void grep() throws MalformedURLException, IOException {
      // if not open yet, open
      if (httpConnection == null)
         openConnection();

      // chained streams - from the initial content input stream into a buffered input stream and then into a stream reader and then a buffered stream reader
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            // set the buffered input stream size to the provided content length
            new BufferedInputStream(httpConnection.getInputStream(), httpConnection.getContentLength()), getBodyEncoding()))) {
         // check each line within the buffered input stream for the search string
         int lineNum = 1;
         for (String line = reader.readLine(); line != null; line = reader.readLine(), lineNum++) {
            // if line has search string, print
            if (line.contains(searchString)) {
               System.out.println("line: " + lineNum + " " + line);
            }
         }
      }
   }

   /**
    * Returns the encoding to be used with the stream reader by analyzing he http header, defaults to UTF-8 if not found
    *
    * @return The httpConnection response encoding
    */
   private String getBodyEncoding() {
      String contentType = httpConnection.getContentType(); // gets the content type
      if (contentType == null || contentType.indexOf("charset=") == -1) { // check if char set specified
         System.out.println("encoding can not be detected - setting to UTF-8");
         return UTF8;
      }
      // else parse out the provided char set
      return contentType.substring(contentType.indexOf("charset=") + "charset=".length());
   }

   /**
    * Main for source viwer, instantiates and runs a SourceViewer and prints out any errors that may occur.
    *
    * @param args Should contain a resolvable http host, and a search string
    */
   public static void main(String[] args) {
      long start = System.currentTimeMillis(); // see how long it takes
      try {
         if (args.length != 2)
            throw new Exception("Souce Viewer takes 2 arguments: IP and a search string");

         new SourceViewer(args[0], args[1]).grep(); // instantiate with args and run
      } catch (MalformedURLException ex) {
         System.err.println(args[0] + " is not a parseable URL");
      } catch (Exception ex) {
         System.err.println(ex);
      }
      System.out.println("Completed in: " + (System.currentTimeMillis() - start));
   }
}
