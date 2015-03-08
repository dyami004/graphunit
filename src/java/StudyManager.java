
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import userstudy.EvaluationQuestion;
import userstudy.MyUtils;
//for xml file reading stuff
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import userstudy.QualitativeQuestion;
import userstudy.StudyParameters;

/**
 *
 * @author Mershack
 */
@WebServlet(urlPatterns = {"/StudyManager"})
public class StudyManager extends HttpServlet {

    private final String DATA_DIR = "data";

    private final String QUANT_QNS_FILENAME = "quantitativeQuestions.txt";
    private final String TASKS_NODES_FILENAME = "taskNodesIndexes.txt";//"tasksNodes.txt";

    HashMap<String, StudyParameters> usersStudyParameters = new HashMap<String, StudyParameters>();

//    /ArrayList<String> taskNodes = new ArrayList<String>();
    //HashMap<Integer, ArrayList<String>> allTaskNodes = new HashMap<Integer, ArrayList<String>>(); //this hashmap will be used
    //HashMap<Integer, String> allQuestions = new HashMap<Integer, String>();
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        //  System.out.println("*** Size of evalquestions is ::: " + evalQuestions.size() + " && viewers are::: " + viewerConditionUrls.size());
        try {

            HttpSession session = request.getSession();
            String message = "";
            //    Environment e = null;
            //get the environment from the session or create a new one if the session doesn't have the environment initialized
            RequestDispatcher view = request.getRequestDispatcher("userstudy.html");

            if (request.getParameter("studyname") != null) {
                String studyname = request.getParameter("studyname");
                //  System.out.println("///////NULL SESSION");c
                session.setAttribute("studyname", studyname);

                // don't add your web-app name to the path
                view.forward(request, response);

                // }
            } else {

                String msg = "Finished";
                String command = request.getParameter("command");
                String nameofstudy;
                String studyId;

                System.out.println("__the command is " + command);

                if (command.equalsIgnoreCase("instruction")) {
                    //for the first time, get the name of the study from the session otherwise expect it to be passed
                    nameofstudy = session.getAttribute("studyname").toString();
                } else {

                    //  System.out.println("QUERY STRING IS:: "+ request.getQueryString());
                    // System.out.println("command---- "+command);
                    //System.out.println(request.getAttribute("studyid"));
                    //    System.out.println("--------------");
                    nameofstudy = request.getParameter("studyid").toString();
                    //  System.out.println();
                }

                //String nameofstudy = session.getAttribute("studyname").toString();
                studyId = session.getId() + nameofstudy;

                //get the user's study parameters.
                StudyParameters upmts = (StudyParameters) usersStudyParameters.get(studyId); // get the user's specific parameters

                if (upmts == null) {//first time, initialize the variable
                    upmts = new StudyParameters();
                    // System.out.println("**NEW**");

                }
                System.out.println("---The command is " + command);

                //  else{
                upmts.studyname = nameofstudy;
                //   }  

                //System.out.println("^^^^ The study id is +++++ " + studyId);
                if (command.equalsIgnoreCase("instruction")) {
                    loadStudyDetails(request, upmts);  //load the study details
                    upmts.viewersChanged = false;
                    upmts.testCounter = 0;
                    upmts.tutorialCounter = 0;
                    upmts.viewerConditionCounter = 0;
                    msg = getInstruction(upmts); //get the instruction
                    //now append the study name to the study so that it can be returned later.
                    msg += "::" + nameofstudy;
                    // System.out.println("INSTRUCTION IS " + msg);
                } else if (command.equalsIgnoreCase("getPreQualitativeQuestions")) {
                    //send the qualitative questions if there is some, otherwise send an empty string
                    String allqualQuestions = "";

                    //System.out.println("The size of preQual is " + upmts.qualEvalQuestionBefore.size());
                    for (int i = 0; i < upmts.qualEvalQuestionBefore.size(); i++) {
                        if (i == 0) {
                            allqualQuestions = upmts.qualQuestionsBefore.get(i) + ":::" + upmts.qualEvalQuestionBefore.get(i).getAnsDetailsAsString();
                        } else {
                            allqualQuestions += "::::" + upmts.qualQuestionsBefore.get(i) + ":::" + upmts.qualEvalQuestionBefore.get(i).getAnsDetailsAsString();
                        }
                    }
                    System.out.println("allQualQuestions::: " + allqualQuestions);
                    if (!allqualQuestions.isEmpty()) {
                        allqualQuestions = "Qualitative::::" + allqualQuestions;
                    }
                    msg = allqualQuestions;
                } else if (command.equalsIgnoreCase("firstViewerUrl")) {
                    msg = upmts.viewerConditionUrls.get(upmts.viewerConditionCounter);
                    upmts.viewerConditionCounter++;
                } else if (command.equalsIgnoreCase("getDataset")) {
                    msg = upmts.dataseturl;
                }
                else if(command.equalsIgnoreCase("prepareQuestions")){
                    prepareQuantitativeQuestions(request, upmts);
                }
                else if(command.equalsIgnoreCase("getViewerDimensions")){
                      String width = upmts.viewerWidth;
                      String height = upmts.viewerHeight;
                      
                      msg = width +"x" + height;  //i.e. w x h  e.g. 800x600   
                }                
                else if (command.equalsIgnoreCase("getQuestion")) {
                    if (upmts.tutorialCounter < upmts.tutorialQuestions.size()) {
                        upmts.isTutorial = true;
                        upmts.tutorialCounter++;
                        msg = "Training Question (" + upmts.tutorialCounter + "/" + upmts.tutorialQuestions.size() + ")"
                                + ":: " + upmts.tutorialQuestions.get(upmts.tutorialCounter - 1).getQuestion();
                        msg += "::" + upmts.tutorialQuestions.get(upmts.tutorialCounter - 1).getMaxTimeInSeconds();  //add the time also
                    } else if (upmts.studyType.equalsIgnoreCase("Within") && (upmts.testCounter > 0 && upmts.testCounter < upmts.evalQuestions.size())
                            && ((upmts.testCounter % upmts.sizeOfACondition) == 0) && !upmts.viewersChanged) {
                        msg = "ChangeViewers:: " + upmts.viewerConditionUrls.get(upmts.viewerConditionCounter);

                        System.out.println("ChangeViewer string is ::: " + msg);
                        upmts.viewerConditionCounter++;
                        upmts.viewersChanged = true;

                    } else if (upmts.testCounter < upmts.evalQuestions.size()) {
                        if (upmts.testCounter > 0) {//get the previousAnswer
                            String prevAnswer = request.getParameter("previousAnswer");
                            String prevTime = request.getParameter("previousTime");

                            System.out.println("Previous Time is ::: " + prevTime);
                            int previousTime = Integer.parseInt(prevTime);
                            upmts.evalQuestions.get(upmts.testCounter - 1).setIsGivenAnsCorrect(prevAnswer.trim());
                            upmts.evalQuestions.get(upmts.testCounter - 1).setTimeInSeconds(previousTime);
                        }
                        upmts.isTutorial = false;
                        upmts.testCounter++;
                        upmts.viewersChanged = false;
                        msg = "Study Question (" + upmts.testCounter + "/" + upmts.evalQuestions.size() + ")";
                        msg += "::  " + upmts.evalQuestions.get(upmts.testCounter - 1).getQuestion(); //the question
                        msg += "::" + upmts.evalQuestions.get(upmts.testCounter - 1).getMaxTimeInSeconds();  //add the time also
                    } else {
                        String prevAnswer = request.getParameter("previousAnswer");
                        String prevTime = request.getParameter("previousTime");

                        System.out.println("Previous Time is ::: " + prevTime);
                        int previousTime = Integer.parseInt(prevTime);

                        upmts.evalQuestions.get(upmts.testCounter - 1).setIsGivenAnsCorrect(prevAnswer.trim());
                        upmts.evalQuestions.get(upmts.testCounter - 1).setTimeInSeconds(previousTime);

                        // writeAnswersToFile(upmts);
                        //find the qualitative questions and send them out, if there are none then send the finished msg
                        if (upmts.qualEvalQuestionAfter.size() == 0) {
                            writeAnswersToFile(upmts);
                            String studyNameReverse = new StringBuffer(upmts.studyname.toUpperCase()).reverse().toString();
                            msg = "Finished::" + upmts.turkCode + studyNameReverse;
                        } else {
                                //send the qualitative questions                          

                            //get the qualitative questions
                            String allqualQuestions = "";
                            for (int i = 0; i < upmts.qualEvalQuestionAfter.size(); i++) {

                                if (i == 0) {
                                    allqualQuestions = upmts.qualQuestionsAfter.get(i) + ":::" + upmts.qualEvalQuestionAfter.get(i).getAnsDetailsAsString();
                                } else {
                                    allqualQuestions += "::::" + upmts.qualQuestionsAfter.get(i) + ":::" + upmts.qualEvalQuestionAfter.get(i).getAnsDetailsAsString();
                                }

                            }
                            msg = "Feedback::::" + allqualQuestions;
                        }
                    }
                } else if (command.equalsIgnoreCase("setPreQualitativeAnswers")) {
                    String qualAnswers = request.getParameter("preQualitativeAnswers");
                    String split[] = qualAnswers.split("::::");
                    //    System.out.println("__PRE-QUALITATIVE ANSWERS ::: ");
                    for (int i = 0; i < upmts.qualEvalQuestionBefore.size(); i++) {
                        upmts.qualEvalQuestionBefore.get(i).setAnswer(split[i]);
                        //System.out.println(upmts.qualEvalQuestionBefore.get(i).getAnswer());
                    }
                } else if (command.equalsIgnoreCase("setQualitativeAnswers")) {
                    //set the qualitative answer and send the turk code
                    String qualAnswers = request.getParameter("qualitativeAnswers");
                    //    System.out.println("_____ Post-Qualitative Answers are ::: " + qualAnswers);

                    String split[] = qualAnswers.split("::::");

                    for (int i = 0; i < upmts.qualEvalQuestionAfter.size(); i++) {
                        upmts.qualEvalQuestionAfter.get(i).setAnswer(split[i]);
                        //  System.out.println(upmts.qualEvalQuestionAfter.get(i).getAnswer() + "****");
                    }
                    writeAnswersToFile(upmts);
                    msg = "Finished::" + upmts.turkCode;

                } else if (command.equalsIgnoreCase("getNodes")) {
                    //get the nodes for that question as string.      
                    if (upmts.isTutorial) {
                        //System.out.println("**** "+ (tutorialCounter - 1));
                        msg = upmts.tutorialQuestions.get(upmts.tutorialCounter - 1).getNodesAsString();
                    } else {
                        msg = upmts.evalQuestions.get(upmts.testCounter - 1).getNodesAsString();
                    }
                } else if (command.equalsIgnoreCase("getAnswerControllers")) {
                    if (upmts.isTutorial) {
                        //System.out.println("**** "+ (tutorialCounter - 1));
                        msg = upmts.tutorialQuestions.get(upmts.tutorialCounter - 1).getAnsOptionsAsString();
                    } else {
                        msg = upmts.evalQuestions.get(upmts.testCounter - 1).getAnsOptionsAsString();
                    }
                } else if (command.equalsIgnoreCase("checkIsTutorial")) {
                    if (upmts.tutorialCounter < upmts.tutorialQuestions.size()) {
                        msg = "true";
                    } else {
                        msg = "false";
                    }
                } else if (command.equalsIgnoreCase("checkAnswer")) {
                    //check if the given answer is right, return "Correct" if right or "Wrong" if wrong
                    String givenAns = request.getParameter("givenAnswer").trim();
                    String correctAns = upmts.tutorialQuestions.get(upmts.tutorialCounter - 1).getCorrectAns(); //NB: check answer is only for tutorials

                    if (givenAns.equalsIgnoreCase(correctAns)) {
                        msg = "Correct!";
                    } else {
                        msg = "Wrong";
                    }

                    // System.out.println("The correctness message is ::: "+ msg);
                } else if (command.equalsIgnoreCase("getNodePositions")) {
                //read the node positions file as a string and send it to the vie

                    //String posFilename = "positions2.txt";
                    //String posFilename = nodePosi;
                    File posFile = new File(getServletContext().getRealPath(upmts.nodePositions));
                    BufferedReader br = new BufferedReader(new FileReader(posFile));
                    String line = "";
                    //ArrayList<String> taskAccuracy = new ArrayList<String>();
                    int cnt = 0;
                    String alldata = "";
                    line = br.readLine(); //this is the header. For now I will not be including it.
                    while ((line = br.readLine()) != null) {
                        String[] split = line.split("\t");
                        //String name = split[0];
                        if (cnt > 0) {
                            alldata += "::::" + split[0] + "::" + split[1] + "::" + split[2];
                        } else {
                            alldata = split[0] + "::" + split[1] + "::" + split[2];
                        }
                        cnt++;
                    }

                    br.close();

                    msg = alldata;
                }

                //put the user study paramters object into the hashtable
                usersStudyParameters.put(studyId, upmts);

                out = response.getWriter();
                out.write(msg);
                out.flush();
                out.close();
            }

        } finally {
            out.close();
        }
    }

    public void printEvaluationAnswers(StudyParameters upmts) {

        System.out.println("Evaluation Answers");
        for (int i = 0; i < upmts.evalQuestions.size(); i++) {
            System.out.println(":::" + upmts.evalQuestions.get(i).getIsGivenAnsCorrect());
        }
    }

    public String getInstruction(StudyParameters upmts) {
        String instruction = "";

        if (upmts.questionCodes.size() > 1) {
            instruction = "Instruction about the tasks::In this study there are " + upmts.questionCodes.size() + " types of questions.\n\n"
                    + "You will be given a simple training  with " + upmts.tutorialSize + " sample questions of each type. "
                    + "You can check whether your chosen answer is correct or not during the training session.\n\n"
                    + "There are " + upmts.totalNumOfQuestions + " questions in total for the  main study";
        } else {
            instruction = "Instruction about the tasks::In this study there is " + upmts.questionCodes.size() + " type of question.\n\n"
                    + "You will be given a simple training  with " + upmts.tutorialSize + " sample questions. "
                    + "You can check whether your chosen answer is correct or not during the training session.\n\n"
                    + "There are " + upmts.totalNumOfQuestions + " questions in total for the  main study";
        }

        return instruction;
    }

    public void loadStudyDetails(HttpServletRequest request, StudyParameters upmts) {
        //load study type file
        //load quantitative questions file        
        upmts.questionCodes = new ArrayList<String>();
        upmts.questions = new ArrayList<String>();
        upmts.questionSizes = new ArrayList<Integer>();
        upmts.questionMaxTimes = new ArrayList<Integer>();

        //for qual qns after
        upmts.qualQuestionCodesAfter = new ArrayList<String>();
        upmts.qualQuestionsAfter = new ArrayList<String>();
        upmts.qualEvalQuestionAfter = new ArrayList<QualitativeQuestion>();

        //for qual qns before
        upmts.qualQuestionCodesBefore = new ArrayList<String>();
        upmts.qualQuestionsBefore = new ArrayList<String>();
        upmts.qualEvalQuestionBefore = new ArrayList<QualitativeQuestion>();

        upmts.evalQuestions = new ArrayList<EvaluationQuestion>();
        upmts.tutorialQuestions = new ArrayList<EvaluationQuestion>();
        upmts.viewerConditionShortnames = new ArrayList<String>();
        upmts.viewerConditionUrls = new ArrayList<String>();

        upmts.orderOfConditionShortNames = new ArrayList<String>();
        upmts.orderOfConditionUrls = new ArrayList<String>();

        //taskTypes = new ArrayList<String>();
        String datasetname = "";
        try {
            //read the xml file that contains the details about the quantitative questions  

            //get the studyname of the request
            //String url2 = request.getServletPath();
            //url2 = request.getRequestURL().toString();
            // System.out.println("********** URL :: "+ url2);
            //get the studydata url
            String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";

            //System.out.println("----------study data url --"+studydataurl + "--");
            String filename = getServletContext().getRealPath(studydataurl + File.separator + "quantitativeTasks.xml");
            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList taskNode = doc.getElementsByTagName("task");
            NodeList datasetNode = doc.getElementsByTagName("dataset");
            // NodeList datasetTypeNode = doc.getElementsByTagName("datasetType");
            NodeList experimentTypeNode = doc.getElementsByTagName("experimenttype");
            NodeList conditionNode = doc.getElementsByTagName("condition");
            NodeList studynameNode = doc.getElementsByTagName("studyname");
            NodeList qualtaskNode = doc.getElementsByTagName("qualtask");
            NodeList viewerwidthNode = doc.getElementsByTagName("viewerwidth");
            NodeList viewerheightNode = doc.getElementsByTagName("viewerheight");
            
            
            //get the dataseturl
            datasetname = ((Element) datasetNode.item(0)).getTextContent();
            upmts.dataseturl = getServerUrl(request) + ("/datasets/" + datasetname + "/" + datasetname);
            // nodePositions = getServerUrl(request) + ("/datasets/" + datasetname + "/positions.txt");

            upmts.datasetname = datasetname;
            upmts.nodePositions = "datasets" + File.separator + datasetname + File.separator + "positions.txt";

            //get the studyname
            upmts.studyname = ((Element) studynameNode.item(0)).getTextContent();
            //get the experiment type
            upmts.studyType = ((Element) experimentTypeNode.item(0)).getTextContent();
                        
            //get the condition urls and shortnames
            for (int i = 0; i < conditionNode.getLength(); i++) {
                Node nNode = conditionNode.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String conditionurl = eElement.getElementsByTagName("conditionurl").item(0).getTextContent();
                    String conditionshortname = eElement.getElementsByTagName("conditionshortname").item(0).getTextContent();

                    upmts.viewerConditionShortnames.add(conditionshortname);
                    String url = "studies/" + upmts.studyname + "/" + conditionurl;
                    upmts.viewerConditionUrls.add(url);

                }
            }

            upmts.utils = new MyUtils(upmts.viewerConditionShortnames);

            //get the task name, question, size, and time
            upmts.totalNumOfQuestions = 0;
            for (int temp = 0; temp < taskNode.getLength(); temp++) {
                Node nNode = taskNode.item(temp);
                //System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String questionCode = eElement.getElementsByTagName("name").item(0).getTextContent();
                    String question = eElement.getElementsByTagName("question").item(0).getTextContent();
                    String questionSize = eElement.getElementsByTagName("size").item(0).getTextContent();
                    String questionTime = eElement.getElementsByTagName("time").item(0).getTextContent();
                    //taskTypes.add(questionCode);
                    upmts.questionCodes.add(questionCode);
                    upmts.questions.add(question);
                    upmts.questionSizes.add(Integer.parseInt(questionSize));
                    upmts.totalNumOfQuestions +=Integer.parseInt(questionSize);
                    upmts.questionMaxTimes.add(Integer.parseInt(questionTime));
                }
            }
            
            if(upmts.studyType.equalsIgnoreCase("within")){
           //     System.out.println("****Within "+ upmts.viewerConditionShortnames.size());
                upmts.totalNumOfQuestions *=  upmts.viewerConditionShortnames.size();
            }
            
            //viewer dimensions
            String viewerWidth = ((Element) viewerwidthNode.item(0)).getTextContent();
            String viewerHeight = ((Element) viewerheightNode.item(0)).getTextContent();
            upmts.viewerWidth = viewerWidth;
            upmts.viewerHeight = viewerHeight;
            
            
            /**
             * ********************************************************************************
             */
            /**
             * * For qualitative Questions ***
             */
            //get the task name, question,  and other details
            for (int temp = 0; temp < qualtaskNode.getLength(); temp++) {
                Node nNode = qualtaskNode.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String questionCode = eElement.getElementsByTagName("name").item(0).getTextContent();
                    String question = eElement.getElementsByTagName("question").item(0).getTextContent();
                    String position = eElement.getElementsByTagName("qualtaskPos").item(0).getTextContent();

                    if (position.equalsIgnoreCase("after")) {
                        upmts.qualQuestionCodesAfter.add(questionCode);
                        upmts.qualQuestionsAfter.add(question);
                    } else {
                        upmts.qualQuestionCodesBefore.add(questionCode);
                        upmts.qualQuestionsBefore.add(question);
                    }
                }
            }

            //read the qualitative question files
            //after quant task questions
            for (int i = 0; i < upmts.qualQuestionCodesAfter.size(); i++) {
                String xmlname = upmts.qualQuestionCodesAfter.get(i) + ".xml";
                filename = getServletContext().getRealPath("qualtasks" + File.separator + xmlname);

                File xmlFile = new File(filename);
                DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
                Document doc2 = dBuilder2.parse(xmlFile);
                doc.getDocumentElement().normalize();

                taskNode = doc2.getElementsByTagName("answertype");
                String answerType = ((Element) taskNode.item(0)).getTextContent(); //answerType
                int min = -1, max = -1;
                //if answer type is rating, then get the minimum and maximum values
                if (answerType.equalsIgnoreCase("range")) {

                    NodeList minNode = doc2.getElementsByTagName("minimum");
                    NodeList maxNode = doc2.getElementsByTagName("maximum");
                    min = Integer.parseInt(((Element) minNode.item(0)).getTextContent());
                    max = Integer.parseInt(((Element) maxNode.item(0)).getTextContent());
                }
                //get the values of the variables
                String question = upmts.qualQuestionsAfter.get(i);
                QualitativeQuestion qualEvalQn = new QualitativeQuestion(question, answerType);
                if (min > -1 && max > 0) {
                    qualEvalQn.setRangeMinimum(min);
                    qualEvalQn.setRangeMaximum(max);
                }
                //add the qualitativequestion to file
                upmts.qualEvalQuestionAfter.add(qualEvalQn);

            }

            //before quant tasks
            for (int i = 0; i < upmts.qualQuestionCodesBefore.size(); i++) {
                String xmlname = upmts.qualQuestionCodesBefore.get(i) + ".xml";
                filename = getServletContext().getRealPath("qualtasks" + File.separator + xmlname);

                File xmlFile = new File(filename);
                DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
                Document doc2 = dBuilder2.parse(xmlFile);
                doc.getDocumentElement().normalize();

                taskNode = doc2.getElementsByTagName("answertype");
                String answerType = ((Element) taskNode.item(0)).getTextContent(); //answerType
                int min = -1, max = -1;
                //if answer type is rating, then get the minimum and maximum values
                if (answerType.equalsIgnoreCase("range")) {

                    NodeList minNode = doc2.getElementsByTagName("minimum");
                    NodeList maxNode = doc2.getElementsByTagName("maximum");
                    min = Integer.parseInt(((Element) minNode.item(0)).getTextContent());
                    max = Integer.parseInt(((Element) maxNode.item(0)).getTextContent());
                }
                //get the values of the variables
                String question = upmts.qualQuestionsBefore.get(i);
                QualitativeQuestion qualEvalQn = new QualitativeQuestion(question, answerType);
                if (min > -1 && max > 0) {
                    qualEvalQn.setRangeMinimum(min);
                    qualEvalQn.setRangeMaximum(max);
                }
                //add the qualitativequestion to file
                upmts.qualEvalQuestionBefore.add(qualEvalQn);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setFirstCondition(upmts);
        upmts.orderOfConditionShortNames.add(upmts.firstConditionShortName);
        upmts.orderOfConditionUrls.add(upmts.firstConditionUrl);
        setOrderOfConditions(upmts);

        //setOrderOfConditions
    }

    public void prepareQuantitativeQuestions(HttpServletRequest request, StudyParameters upmts) {
        /**
         * read the question nodes *
         */
        try {
            String filename = "";
            
            String graphType = request.getParameter("graphType"); //get the graph type variable
            
                        
            for (int i = 0; i < upmts.questionCodes.size(); i++) {
                String xmlname = upmts.questionCodes.get(i) + ".xml";
                filename = getServletContext().getRealPath("datasets" + File.separator 
                        + upmts.datasetname + File.separator 
                        + graphType + File.separator + xmlname);

                File xmlFile = new File(filename);
                DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
                Document doc2 = dBuilder2.parse(xmlFile);
                doc2.getDocumentElement().normalize();

                NodeList ansTypeNode = doc2.getElementsByTagName("answertype");
                String answerType = ((Element) ansTypeNode.item(0)).getTextContent(); //answerType
                int questionsize = upmts.questionSizes.get(i);               //questionSize
                String question = upmts.questions.get(i);
                int maxTime = upmts.questionMaxTimes.get(i);

                ArrayList<String> options = new ArrayList<String>();
                NodeList optionsNode = doc2.getElementsByTagName("option");
                //add the options of the question 
                for (int k = 0; k < optionsNode.getLength(); k++) {
                    String ansOption = optionsNode.item(k).getTextContent();
                    options.add(ansOption);
                }

                NodeList questionNode = doc2.getElementsByTagName("question");
                int questionCount = 0;
                int tutorialCount = 0;
                for (int temp = 0; temp < questionNode.getLength(); temp++) {
                    Node nNode = questionNode.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        //get the answers
                        String answer = eElement.getElementsByTagName("answer").item(0).getTextContent();
                        //System.out.println("ANSWER -> " + answer);
                        //get the nodes
                        ArrayList<String> nodes = new ArrayList<String>();
                        NodeList nodeList = eElement.getElementsByTagName("node");

                        for (int j = 0; j < nodeList.getLength(); j++) {
                            String nodeText = nodeList.item(j).getTextContent();
                            //System.out.println("Node "+(j+1) + " is " + nodeText);
                            nodes.add(nodeText);
                        }
                        System.out.println();

                        EvaluationQuestion evalQn = new EvaluationQuestion(question, answer, nodes, options, answerType, maxTime);
                        //add the question to either the tutorial list or the test list
                        if (tutorialCount < upmts.tutorialSize) {
                            upmts.tutorialQuestions.add(evalQn);
                            tutorialCount++;
                        } else {
                            upmts.evalQuestions.add(evalQn);
                            questionCount++;
                        }

                        if (questionCount == questionsize) {
                            break;
                        }
                    }
                }
            }

            /**
             * adjust the tasks if it is a within user study
             */
            if (upmts.studyType.equalsIgnoreCase("Within")) {
                adjustTasksForWithinStudy(upmts);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setOrderOfConditions(StudyParameters upmts) {
        //start from the first condition and add the other conditions in 
        //a round-robin kind of fashion
        ArrayList<String> orderdedVCurls = new ArrayList<String>();
        ArrayList<String> orderdedVCshortn = new ArrayList<String>();

        int index = -1;
        //find the index of the first condition among the conditions
        for (int i = 0; i < upmts.viewerConditionShortnames.size(); i++) {
            if (upmts.firstConditionShortName.equalsIgnoreCase(upmts.viewerConditionShortnames.get(i))) {
                index = i;
                break;
            }
        }

        int cnt = 0;
        int size = 0;
        if (index >= 0) {
            cnt = index;
            while (upmts.orderOfConditionShortNames.size() < upmts.viewerConditionShortnames.size()) {
                cnt++;
                if (cnt == upmts.viewerConditionUrls.size()) {
                    cnt = 0;
                }

                upmts.orderOfConditionShortNames.add(upmts.viewerConditionShortnames.get(cnt));
                upmts.orderOfConditionUrls.add(upmts.viewerConditionUrls.get(cnt));
            }
        }

        upmts.viewerConditionUrls = new ArrayList<String>();
        for (int i = 0; i < upmts.orderOfConditionUrls.size(); i++) {
            upmts.viewerConditionUrls.add(upmts.orderOfConditionUrls.get(i));
        }
        //System.out.println("the condition URLs are ::: "+ orderOfConditionUrls);
    }

    /**
     * This method will determine which condition among the other options will
     * be made first.
     */
    public void setFirstCondition(StudyParameters upmts) {
        //TODO: read a file that contains  the completed studies compare them to the condition,
        //and select the condition with the lower count
        try {

            ArrayList<String> firstConditions = new ArrayList<String>();
            int[] conditionCount = new int[upmts.viewerConditionShortnames.size()];
            //initialize the count values
            for (int i = 0; i < conditionCount.length; i++) {
                conditionCount[i] = 0;
            }

            String filename = "firstConditions.txt";
            String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
            File file = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            //ArrayList<String> taskAccuracy = new ArrayList<String>();
            while ((line = br.readLine()) != null) {

                for (int i = 0; i < upmts.viewerConditionShortnames.size(); i++) {
                    if (line.trim().equalsIgnoreCase(upmts.viewerConditionShortnames.get(i))) {
                        conditionCount[i]++;
                        break;
                    }
                }
            }

            br.close();

            //find one of the condition with the minimum count
            int minIndex = 0;
            int minimumCount = conditionCount[0];
            for (int i = 1; i < conditionCount.length; i++) {
                if (conditionCount[i] < minimumCount) {
                    minIndex = i;
                    minimumCount = conditionCount[i];
                }
            }
            upmts.firstConditionShortName = upmts.viewerConditionShortnames.get(minIndex);
            upmts.firstConditionUrl = upmts.viewerConditionUrls.get(minIndex);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void adjustTasksForWithinStudy(StudyParameters upmts) {
        //we will double/triple/etc the testtasks, i.e. add the same tasks to the end of the list for each of the conditions.
        int length = upmts.evalQuestions.size();

        upmts.sizeOfACondition = length;

        //we will be having the same question for all the conditions
        for (int k = 0; k < upmts.viewerConditionUrls.size() - 1; k++) {
            for (int i = 0; i < length; i++) {
                EvaluationQuestion evq = upmts.evalQuestions.get(i);
                EvaluationQuestion evalQ = new EvaluationQuestion(evq.getQuestion(), evq.getCorrectAns(), evq.getNodes(),
                        evq.getAnsOptions(), evq.getAnsType(), evq.getMaxTimeInSeconds());

                upmts.evalQuestions.add(evalQ);
            }
        }
    }

    public void printAllNodesInvolved(StudyParameters upmts) {
        for (EvaluationQuestion eq : upmts.evalQuestions) {
            ArrayList<String> nodes = eq.getNodes();

            for (String str : nodes) {
                System.out.print(str + ", ");
            }
            System.out.println(eq.getCorrectAns());
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void writeAnswersToFile(StudyParameters upmts) {
        try {
            //String cond1_resultsFile = "condition1_results.txt";
            //String cond2_resultsFile = "condition2_results.txt";

            //  System.out.println("Writing Results to File");
            // orderOfConditionShortNames = new ArrayList<String>();
            // orderOfConditionShortNames.add("cond1");
            // orderOfConditionShortNames.add("cond2");
            String filename = "";

            System.out.println("The StudyType is ***" + upmts.studyType + "*");

            if (upmts.studyType.equalsIgnoreCase("Between")) {
                upmts.currentCondition = upmts.firstConditionShortName;
                writeBetweenStudyAnwsersToFile(upmts);
            } else if (upmts.studyType.equalsIgnoreCase("Within")) {
                writeWithinStudyAnswersToFile(upmts);
            }

            //write the first condition also to file
            writeFirstConditionToFile(upmts);

            writeQualitativeAnswersToFile(upmts);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeQualitativeAnswersToFile(StudyParameters upmts) {
        //write the pre-qualitative questions and the postqualitative questions to file
        try {
            String filename = "QualitativeAnswers.txt";
            String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
            File file = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));

            boolean newFile = false;

            if (!file.exists()) {
                file.createNewFile();
                newFile = true;
            }
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            //first write the questions as headers
            String question, answer;
            if (newFile) { //print the header
                for (int i = 0; i < upmts.qualEvalQuestionBefore.size(); i++) {
                    question = upmts.qualEvalQuestionBefore.get(i).getQuestion();
                    if (i == 0) {
                        pw.print(question);
                    } else {
                        pw.print("\t\t" + question);
                    }
                }
                for (int i = 0; i < upmts.qualEvalQuestionAfter.size(); i++) {
                    question = upmts.qualEvalQuestionAfter.get(i).getQuestion();

                    if (i == 0 && upmts.qualQuestionCodesBefore.size() == 0) {
                        pw.print(question);
                    } else {
                        pw.print("\t\t" + question);
                    }
                }
                pw.println();
            }

            //write the answers
            for (int i = 0; i < upmts.qualEvalQuestionBefore.size(); i++) {
                answer = upmts.qualEvalQuestionBefore.get(i).getAnswer();
                if (i == 0) {
                    pw.print(answer);
                } else {
                    pw.print("\t\t" + answer);
                }
            }
            for (int i = 0; i < upmts.qualEvalQuestionAfter.size(); i++) {
                answer = upmts.qualEvalQuestionAfter.get(i).getAnswer();

                System.out.println("Answer for the post Qual is " + answer);

                if (i == 0 && upmts.qualEvalQuestionBefore.isEmpty()) {
                    pw.print(answer);
                } else {
                    pw.print("\t\t" + answer);
                }
            }
            pw.println();

            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void writeBetweenStudyAnwsersToFile(StudyParameters upmts) {
        //write a between study results to file
        try {
            System.out.println("Writing Between Study Results");

            String filename_accuracy = upmts.utils.getConditionAccuracyFileName(upmts.currentCondition);
            String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
            File resultFile_accuracy = new File(getServletContext().getRealPath(studydataurl + File.separator + filename_accuracy));
            boolean newFile_acc = false;
            if (!resultFile_accuracy.exists()) {
                resultFile_accuracy.createNewFile();
                newFile_acc = true;
            }
            //do the actual writings of the results to the file
            FileWriter fw1 = new FileWriter(resultFile_accuracy, true);
            BufferedWriter bw1 = new BufferedWriter(fw1);

            PrintWriter pw1 = new PrintWriter(bw1);

            if (newFile_acc) {
                //write the headers
                pw1.print("Acc_" + upmts.questionCodes.get(0) + "_" + upmts.currentCondition);

                for (int i = 1; i < upmts.questionCodes.size(); i++) {
                    pw1.print("," + "Acc_" + upmts.questionCodes.get(i) + "_" + upmts.currentCondition);
                }
                pw1.println();
            }
            int j = 0;
            int taskSize = upmts.questionSizes.get(j);
            int cnt = 0;
            int numCorrect = 0;

            for (int i = 0; i < upmts.evalQuestions.size(); i++) {
                cnt++;
                if (!(cnt <= taskSize)) {
                    // taskCorrectness.add(cnt);
                    if (j == 0) {
                        pw1.print((double) numCorrect / taskSize);
                    } else {
                        pw1.print("," + (double) numCorrect / taskSize);
                    }
                    taskSize = (Integer) upmts.questionSizes.get(j);
                    j++;
                    cnt = 0;
                    numCorrect = 0;
                }
                if (upmts.evalQuestions.get(i).getIsGivenAnsCorrect()) {
                    numCorrect++;
                }
            }
            //pw1.print("," + (double) numCorrect / taskSize);
            if (j == 0) { //only one question
                pw1.print((double) numCorrect / taskSize);
            } else {
                pw1.print("," + (double) numCorrect / taskSize);
            }

            pw1.println();
            pw1.close();

            /*Write time to file */
            String filename_time = upmts.utils.getConditionTimeFileName(upmts.currentCondition);
            // String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";

            File resultFile_time = new File(getServletContext().getRealPath(studydataurl + File.separator + filename_time));
            boolean newFile_time = false;
            if (!resultFile_time.exists()) {
                resultFile_time.createNewFile();
                newFile_time = true;
            }

            FileWriter fw2 = new FileWriter(resultFile_time, true);
            BufferedWriter bw2 = new BufferedWriter(fw2);

            PrintWriter pw2 = new PrintWriter(bw2);

            if (newFile_time) {
                //write the headers
                pw2.print("Time_" + upmts.questionCodes.get(0) + "_" + upmts.currentCondition);
                System.out.print("Time_" + upmts.questionCodes.get(0) + "_" + upmts.currentCondition);

                for (int i = 1; i < upmts.questionCodes.size(); i++) {
                    pw2.print("," + "Time_" + upmts.questionCodes.get(i) + "_" + upmts.currentCondition);
                    System.out.print("," + "Time_" + upmts.questionCodes.get(i) + "_" + upmts.currentCondition);
                }
                pw2.println();
            }
            int k = 0;
            int taskSize_time = upmts.questionSizes.get(k);
            int cnt2 = 0;
            int totalTime = 0;

            for (int i = 0; i < upmts.evalQuestions.size(); i++) {
                cnt2++;
                if (!(cnt2 <= taskSize_time)) {
                    if (k == 0) {
                        pw2.print((double) totalTime / taskSize_time);

                        System.out.print((double) totalTime / taskSize_time);
                    } else {
                        pw2.print("," + (double) totalTime / taskSize_time);
                        System.out.print("," + (double) totalTime / taskSize_time);
                    }
                    taskSize_time = (Integer) upmts.questionSizes.get(k);
                    k++;
                    cnt2 = 0;
                    totalTime = 0;
                }

                totalTime += upmts.evalQuestions.get(i).getTimeInSeconds();
            }

            if (j == 0) { //only one question
                pw2.print((double) totalTime / taskSize_time);
            } else {
                pw2.print("," + (double) totalTime / taskSize_time);
            }
            //System.out.print("," + (double) totalTime / taskSize_time);
            pw2.println();
            pw2.close();

            /**
             * * Write the basic raw data also to file Starting with the
             * Accuracy
             *
             */
            int start = 0;
            taskSize = 0;
            /**
             * First write the file headers *
             */
            String filename_bacc = upmts.utils.getConditionAccuracyBasicFileName(upmts.currentCondition);
//            /  String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
            File file_bacc = new File(getServletContext().getRealPath(studydataurl + File.separator + filename_bacc));

            boolean newFile = false;

            if (!file_bacc.exists()) {
                file_bacc.createNewFile();
                newFile = true;
                //print the headers
            }

            BufferedWriter bw_bacc = new BufferedWriter(new FileWriter(file_bacc, true));
            PrintWriter pw_bacc = new PrintWriter(bw_bacc);

            if (newFile) {
                start = 0;
                taskSize = 0;
                for (int m = 0; m < upmts.questionCodes.size(); m++) {
                    start = taskSize;
                    taskSize = upmts.questionSizes.get(m);
                    int limit = start + taskSize;
                    cnt = 0;

                    for (int n = start; n < limit; n++) {

                        String name = "Acc_" + upmts.questionCodes.get(m) + "_" + upmts.currentCondition;

                        if (n == 0 && start == 0) {
                            pw_bacc.print(name);
                        } else if (cnt == 0) {
                            pw_bacc.print(" :: " + name);
                        } else {
                            pw_bacc.print(",");
                        }
                        cnt++;
                    }
                }
                pw_bacc.println();

                /**
                 * *************************************************
                 */
                //The question headers i.e. Q1, Q2, etc.
                start = 0;
                taskSize = 0;
                for (int m = 0; m < upmts.questionCodes.size(); m++) {
                    start = taskSize;
                    taskSize = upmts.questionSizes.get(m);
                    int limit = start + taskSize;
                    cnt = 0;
                    for (int n = start; n < limit; n++) {
                        String name = "Q" + (cnt + 1);
                        if (n == 0 && start == 0) {
                            pw_bacc.print(name);
                        } else if (cnt == 0) {
                            pw_bacc.print(" :: " + name);
                        } else {
                            pw_bacc.print("," + name);
                        }
                        cnt++;
                    }
                }
                pw_bacc.println();
            }

            /**
             * Now write the answers given for each question *
             */
            j = 0;
            cnt = 0;
            taskSize = upmts.questionSizes.get(j);

            for (int m = 0; m < upmts.evalQuestions.size(); m++) {
                cnt++;
                boolean ans = upmts.evalQuestions.get(m).getIsGivenAnsCorrect();

                if (j == 0 && cnt == 1) {
                    pw_bacc.print(ans);
                } else if (cnt == 1) {
                    pw_bacc.print(" :: " + ans);
                } else {
                    pw_bacc.print("," + ans);
                }
                if (cnt == taskSize) {
                    taskSize = (Integer) upmts.questionSizes.get(j);
                    j++;
                    cnt = 0;
                }

            }
            pw_bacc.println();

            //close the printWriters
            pw_bacc.close();

            /**
             * *
             * Write the raw time data also to file. It follows the same format
             * as the accuracy
             */
            String filename_btime = upmts.utils.getConditionTimeBasicFileName(upmts.currentCondition);

            File file_btime = new File(getServletContext().getRealPath(studydataurl + File.separator + filename_btime));

            newFile = false;

            if (!file_btime.exists()) {
                file_btime.createNewFile();
                newFile = true;
                //print the headers
            }

            BufferedWriter bw_btime = new BufferedWriter(new FileWriter(file_btime, true));
            PrintWriter pw_btime = new PrintWriter(bw_btime);

            if (newFile) {
                start = 0;
                taskSize = 0;
                for (int m = 0; m < upmts.questionCodes.size(); m++) {
                    start = taskSize;
                    taskSize = upmts.questionSizes.get(m);
                    int limit = start + taskSize;
                    cnt = 0;

                    for (int n = start; n < limit; n++) {
                        String name = "Time_" + upmts.questionCodes.get(m) + "_" + upmts.currentCondition;

                        if (n == 0 && start == 0) {
                            pw_btime.print(name);
                        } else if (cnt == 0) {
                            pw_btime.print(" :: " + name);
                        } else {
                            pw_btime.print(",");
                        }
                        cnt++;
                    }
                }
                pw_btime.println();

                /**
                 * *************************************************
                 */
                //The question headers i.e. Q1, Q2, etc.
                start = 0;
                taskSize = 0;
                for (int m = 0; m < upmts.questionCodes.size(); m++) {
                    start = taskSize;
                    taskSize = upmts.questionSizes.get(m);
                    int limit = start + taskSize;
                    cnt = 0;
                    for (int n = start; n < limit; n++) {
                        String name = "Q" + (cnt + 1);
                        if (n == 0 && start == 0) {
                            pw_btime.print(name);
                        } else if (cnt == 0) {
                            pw_btime.print(" :: " + name);
                        } else {
                            pw_btime.print("," + name);
                        }
                        cnt++;
                    }
                }
                pw_btime.println();
            }

            /**
             * Now write the answers given for each question *
             */
            j = 0;
            cnt = 0;
            taskSize = upmts.questionSizes.get(j);

            for (int m = 0; m < upmts.evalQuestions.size(); m++) {
                cnt++;
                int time = upmts.evalQuestions.get(m).getTimeInSeconds();

                if (j == 0 && cnt == 1) {
                    pw_btime.print(time);
                } else if (cnt == 1) {
                    pw_btime.print(" :: " + time);
                } else {
                    pw_btime.print("," + time);
                }
                if (cnt == taskSize) {
                    taskSize = (Integer) upmts.questionSizes.get(j);
                    j++;
                    cnt = 0;
                }

            }
            pw_btime.println();

            //close the printWriter
            pw_btime.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeWithinStudyAnswersToFile(StudyParameters upmts) {
        // System.out.println("WRITING WITHIN STUDY +++ ");
        try {
            int numOfConditions = upmts.orderOfConditionShortNames.size();
            //String filenames[] = new String[numOwriteWithinStudyAnswersToFilefConditions];
            File files[] = new File[numOfConditions];
            BufferedWriter bws[] = new BufferedWriter[numOfConditions];
            PrintWriter pws[] = new PrintWriter[numOfConditions];

            String filename;
            for (int i = 0; i < numOfConditions; i++) {
                filename = upmts.utils.getConditionAccuracyFileName(upmts.orderOfConditionShortNames.get(i));
                String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
                files[i] = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));

                boolean newFile = false;

                if (!files[i].exists()) {
                    files[i].createNewFile();
                    newFile = true;

                }

                bws[i] = new BufferedWriter(new FileWriter(files[i], true));
                pws[i] = new PrintWriter(bws[i]);

                if (newFile) {
                    //write the headers
                    upmts.currentCondition = upmts.orderOfConditionShortNames.get(i);
                    pws[i].print("Acc_" + upmts.questionCodes.get(0) + "_" + upmts.currentCondition);

                    for (int j = 1; j < upmts.questionCodes.size(); j++) {
                        pws[i].print("," + "Acc_" + upmts.questionCodes.get(j) + "_" + upmts.currentCondition);
                    }
                    pws[i].println();
                }
            }

            //write the data to a file.
            int start = 0;
            int limit = 0;
            for (int i = 0; i < numOfConditions; i++) {
                start = i * upmts.sizeOfACondition;
                limit = start + upmts.sizeOfACondition;

                int j = 0;
                int taskSize = upmts.questionSizes.get(j);
                int cnt = 0;
                int numCorrect = 0;

                for (int k = start; k < limit; k++) {
                    //  System.out.println("THE VALUEOF J IS;: " + j);
                    cnt++;
                    if (!(cnt <= taskSize)) {
                        // System.out.println("THE VALUEOF J IS;: " + j);
                        if (j == 0) {
                            pws[i].print((double) numCorrect / taskSize);
                        } else {
                            pws[i].print("," + (double) numCorrect / taskSize);
                        }
                        taskSize = (Integer) upmts.questionSizes.get(j);
                        j++;
                        cnt = 0;
                        numCorrect = 0;
                    }
                    if (upmts.evalQuestions.get(k).getIsGivenAnsCorrect()) {
                        numCorrect++;
                    }
                }
                if (j == 0) { //only one question
                    pws[i].print((double) numCorrect / taskSize);
                } else {
                    pws[i].print("," + (double) numCorrect / taskSize);
                }
                pws[i].println();
            }
            System.out.println("Results written to file successfully!");

            //close the printWriters
            for (int i = 0; i < pws.length; i++) {
                pws[i].close();
            }

            /**
             * *********************************************************
             * *********************************************************
             */
            /* Write the time also to file */
            for (int i = 0; i < numOfConditions; i++) {
                filename = upmts.utils.getConditionTimeFileName(upmts.orderOfConditionShortNames.get(i));
                String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
                files[i] = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));

                boolean newFile = false;

                if (!files[i].exists()) {
                    files[i].createNewFile();
                    newFile = true;
                }

                bws[i] = new BufferedWriter(new FileWriter(files[i], true));
                pws[i] = new PrintWriter(bws[i]);

                if (newFile) {
                    //write the headers
                    upmts.currentCondition = upmts.orderOfConditionShortNames.get(i);
                    pws[i].print("Time_" + upmts.questionCodes.get(0) + "_" + upmts.currentCondition);

                    for (int j = 1; j < upmts.questionCodes.size(); j++) {
                        pws[i].print("," + "Time_" + upmts.questionCodes.get(j) + "_" + upmts.currentCondition);
                    }
                    pws[i].println();
                }
            }

            //write the data to a file.
            start = 0;
            limit = 0;
            for (int i = 0; i < numOfConditions; i++) {
                start = i * upmts.sizeOfACondition;
                limit = start + upmts.sizeOfACondition;

                int j = 0;
                int taskSize = upmts.questionSizes.get(j);
                int cnt = 0;
                int totalTime = 0;

                for (int k = start; k < limit; k++) {
                    cnt++;
                    if (!(cnt <= taskSize)) {
                        if (j == 0) {
                            pws[i].print((double) totalTime / taskSize);
                        } else {
                            pws[i].print("," + (double) totalTime / taskSize);
                        }
                        taskSize = (Integer) upmts.questionSizes.get(j);
                        j++;
                        cnt = 0;
                        totalTime = 0;
                    }

                    totalTime += upmts.evalQuestions.get(k).getTimeInSeconds();
                }

                if (j == 0) { //only one question type
                    pws[i].print((double) totalTime / taskSize);
                } else {
                    pws[i].print("," + (double) totalTime / taskSize);
                }
                pws[i].println();
            }
            System.out.println("Results written to file successfully!");

            //close the printWriters
            for (int i = 0; i < pws.length; i++) {
                pws[i].close();
            }

            /**
             * **************************************
             * **************************************
             * *write the basic raw data also to file
             * *****************************************
             * *****************************************
             * ********************************************
             */
            start = 0;
            int taskSize = 0;
            int cnt = 0;
            /**
             * First write the file headers *
             */
            for (int i = 0; i < numOfConditions; i++) {

                filename = upmts.utils.getConditionAccuracyBasicFileName(upmts.orderOfConditionShortNames.get(i));
                String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
                files[i] = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));

                boolean newFile = false;

                if (!files[i].exists()) {
                    files[i].createNewFile();
                    newFile = true;
                    //print the headers
                }

                bws[i] = new BufferedWriter(new FileWriter(files[i], true));
                pws[i] = new PrintWriter(bws[i]);

                if (newFile) {
                    //write the headers
                    //first line will be the actual name and the second line will be the question numbers
                    upmts.currentCondition = upmts.orderOfConditionShortNames.get(i);

                    start = 0;
                    taskSize = 0;
                    cnt = 0;

                    for (int j = 0; j < upmts.questionSizes.size(); j++) {
                        start = taskSize;
                        taskSize = upmts.questionSizes.get(j);
                        String ttype = upmts.questionCodes.get(j);
                        limit = start + taskSize;
                        cnt = 0;

                        for (int k = start; k < limit; k++) {
                            //ttype = taskTypes.get(m);
                            String name = "Acc_" + ttype + "_" + upmts.currentCondition;
                            if (k == 0 && start == 0) {
                                pws[i].print(name);
                            } else if (cnt == 0) {
                                pws[i].print(" :: " + name);
                            } else {
                                pws[i].print(",");
                            }

                            cnt++;
                        }
                    }

                    pws[i].println();

                    //print the question headers
                    start = 0;
                    taskSize = 0;
                    cnt = 0;
                    for (int j = 0; j < upmts.questionSizes.size(); j++) {
                        start = taskSize;
                        taskSize = upmts.questionSizes.get(j);
                        limit = start + taskSize;
                        cnt = 0;
                        for (int k = start; k < limit; k++) {
                            String name = "Q" + (cnt + 1);
                            if (k == 0 && start == 0) {
                                pws[i].print(name);
                            } else if (cnt == 0) {
                                pws[i].print(" :: " + name);
                            } else {
                                pws[i].print("," + name);
                            }
                            cnt++;
                        }
                    }
                    pws[i].println();

                }
            }

            /**
             * Now write the answers given for each question *
             */
            start = 0;
            limit = 0;
            System.out.println("*** Size of a condition::: " + upmts.sizeOfACondition);
            for (int i = 0; i < numOfConditions; i++) {
                start = i * upmts.sizeOfACondition;
                limit = start + upmts.sizeOfACondition;

                int j = 0;
                taskSize = upmts.questionSizes.get(j);
                cnt = 0;

                //   System.out.println("The Start is:: " + start +"  The LIMIT IS :: " +limit);
                for (int k = start; k < limit; k++) {
                    cnt++;
                    boolean ans = upmts.evalQuestions.get(k).getIsGivenAnsCorrect();

                    if (j == 0 && cnt == 1) {
                        pws[i].print(ans);
                    } else if (cnt == 1) {
                        pws[i].print(" :: " + ans);
                    } else {
                        pws[i].print("," + ans);
                    }

                    if (cnt == taskSize) {
                        taskSize = (Integer) upmts.questionSizes.get(j);
                        j++;
                        cnt = 0;
                    }

                }
                pws[i].println();
            }

            //close the printWriters
            for (int i = 0; i < pws.length; i++) {
                pws[i].close();
            }
            /**
             * **************************************
             * Write the Raw Time data also to file
             * *****************************************
             */
            for (int i = 0; i < numOfConditions; i++) {
                filename = upmts.utils.getConditionTimeBasicFileName(upmts.orderOfConditionShortNames.get(i));
                String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
                files[i] = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));

                boolean newFile = false;

                if (!files[i].exists()) {
                    files[i].createNewFile();
                    newFile = true;
                }

                bws[i] = new BufferedWriter(new FileWriter(files[i], true));
                pws[i] = new PrintWriter(bws[i]);

                start = 0;
                taskSize = 0;
                cnt = 0;

                if (newFile) {
                    //write the headers
                    //first line will be the actual name and the second line will be the question numbers
                    upmts.currentCondition = upmts.orderOfConditionShortNames.get(i);

                    for (int j = 0; j < upmts.questionSizes.size(); j++) {
                        start = taskSize;
                        taskSize = upmts.questionSizes.get(j);
                        String ttype = upmts.questionCodes.get(j);
                        limit = start + taskSize;
                        cnt = 0;

                        for (int k = start; k < limit; k++) {
                            String name = "Time_" + ttype + "_" + upmts.currentCondition;
                            if (k == 0 && start == 0) {
                                pws[i].print(name);
                            } else if (cnt == 0) {
                                pws[i].print(" :: " + name);
                            } else {
                                pws[i].print(",");
                            }
                            cnt++;
                        }
                    }

                    pws[i].println();
                    //print the question headers
                    start = 0;
                    taskSize = 0;
                    cnt = 0;
                    for (int j = 0; j < upmts.questionSizes.size(); j++) {
                        start = taskSize;
                        taskSize = upmts.questionSizes.get(j);
                        limit = start + taskSize;
                        cnt = 0;
                        for (int k = start; k < limit; k++) {
                            String name = "Q" + (cnt + 1);
                            if (k == 0 && start == 0) {
                                pws[i].print(name);
                            } else if (cnt == 0) {
                                pws[i].print(" :: " + name);
                            } else {
                                pws[i].print("," + name);
                            }
                            cnt++;
                        }
                    }
                    pws[i].println();
                }
            }

            /**
             * Now write the time for each question *
             */
            start = 0;
            limit = 0;
            for (int i = 0; i < numOfConditions; i++) {
                start = i * upmts.sizeOfACondition;
                limit = start + upmts.sizeOfACondition;

                int j = 0;
                taskSize = upmts.questionSizes.get(j);
                cnt = 0;

                for (int k = start; k < limit; k++) {
                    cnt++;
                    int time = upmts.evalQuestions.get(k).getTimeInSeconds();

                    if (j == 0 && cnt == 1) {
                        pws[i].print(time);
                    } else if (cnt == 1) {
                        pws[i].print(" :: " + time);
                    } else {
                        pws[i].print("," + time);
                    }

                    if (cnt == taskSize) {
                        taskSize = (Integer) upmts.questionSizes.get(j);
                        j++;
                        cnt = 0;
                    }
                }
                pws[i].println();
            }
            //close the printWriters
            for (int i = 0; i < pws.length; i++) {
                pws[i].close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeFirstConditionToFile(StudyParameters upmts) {

        try {
            String filename = "firstConditions.txt";
            String studydataurl = "studies" + File.separator + upmts.studyname + File.separator + "data";
            File file = new File(getServletContext().getRealPath(studydataurl + File.separator + filename));
            // boolean newFile_acc = false;
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw1 = new FileWriter(file, true);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter pw1 = new PrintWriter(bw1);

            pw1.println(upmts.firstConditionShortName);

            pw1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This function returns the url of the server from the request (*without
     * the name of this servlet*).
     *
     * @param request
     * @return
     */
    public String getServerUrl(HttpServletRequest request) {
        String uri = request.getScheme() + "://" + // "http" + "://
                request.getServerName() + // "myhost"
                ":" + // ":"
                request.getServerPort() + // "8080"
                request.getRequestURI();//+       // "/people"
        // "?" +                           // "?"
        // request.getQueryString(); 

        int lastbackslash = uri.lastIndexOf("/");
        return uri.substring(0, lastbackslash);
    }

}
