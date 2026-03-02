# An Arena for LLMs in Programming Education: Anonymous Model Comparisons for Programming Support by Human Preference Evaluation

## Description
This project serves as an arena for LLMs similar to the previous [work of Chiang et al.
(2024)](https://arxiv.org/abs/2403.04132) who already developed a similar system called [’Chatbot Arena’](https://lmarena.ai/about) , but in our case with a
focus on testing LLMs on their programming educational skill. We will use our system to enable
a quantitative analysis with some of ranked LLMs of chatbot arena to find out which
LLM is the best in a given segment of programming education and if their rankings are similar to ours regarding our selected models.

## Installation
Install the LTS version of [JDK 21 (Adoptium)](https://adoptium.net/de/)  
Install the LTS version 22 of [Node.js](https://nodejs.org/en)  
Run whole project (frontend and backend) by using ```./mvnw``` from the llmape folder  
  
You can as well only start the backend by executing ```./mvnw -P-webapp``` and start the frontend separately using ```npm start```.
This way you can modify the client code and every time you save a file, your webapp gets refreshed and you can see the changes immediately.

## Usage
There are three categories to choose from when prompting the AI models: HINT_GENERATION, EXERCISE_GENERATION, CODE_ASSESSMENT.  
Our program as well explains what each category stands for and an LLM also checks if the submitted prompt fits the given category, otherwise
the prompt will be rejected and the user receives the reason why it was rejected.

Whe a prompt was submitted, the user gets responses from two randomly selected LLMs which are anonymous to the user until the user votes for 
one of the LLMs or decides on a tie. The votes of our users contribute to the leaderboard whose scores are updated each 30 minutes.

For a logged in user, the platform as well provides additional tools to prompt all LLMs at once with no restriction on the prompt, but this
doesn't contribute to the study.

## Features
- Leaderboard with Bradley Terry model for statistical score and rank estimation
- Vote aware selection algorithm prefers battles between models with low amount of battles against each otherwise
- Global rate limiting and per IP rate limiting (both configurable)
    - two tokens needed for one prompt with answer
    - warning email if one of the limits is half/fully exhausted
        - emails wont be flooded (restriction on how many warnings are sended)
- Easily extend to use further LLM vendors
- Dedicated page for logged in users to submit a prompt and compare responses of all selected LLMs
- Dedicated page for admins to generate prompts along with votes for better responses utilizing the registered LLMs
- Data analysis pipeline to get a lot of information about your collected data (src/main/leaderboard/data_analysis.ipynb)

### Currently supported vendors
- OpenAI
- Google
- Mistral
- Anthropic

### How to extend
- add a class for your new provider such as TestProvider that implements our interface AIModelProvider
- add your preferred models to the database including API key using the admin frontend like explained below

## How to build
Build jar file using ```./mvnw -Pprod clean verify``` 

## Deployment
After building the jar file, use ```Docker compose up``` to start three docker containers
- Backend and frontend application
- PostgreSQL database
- Python container that runs the Bradley Terry statistical rank estimation to calculate new scores/ranks as a cronjob each 30 minutes

### Important environment variables to set
- DB_PASSWORD_LLMAPE
- JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET
- GMAIL_CLIENT_ID
- GMAIL_CLIENT_SECRET
- GMAIL_REFRESH_TOKEN
- OPENAI_API_TOKEN (needed for the rejection checking LLM - gpt-4.1-nano - model name can be configured but has to be OpenAI)
- BASE_URL (http://www.llm-ape.at)
- RATE_LIMIT_GLOBAL_PER_HOUR (sending prompt is one token and receiving the answers is one token - set to 1000)
- RATE_LIMIT_PER_IP_PER_HOUR (sending prompt is one token and receiving the answers is one token - set to 350)
    - mind that also different devices can appear to have the same IP!

### Project uses custom gitlab runner!
- This project uses a custom gitlab runner, since the UIBK runners don't allow to connect to the server. 
- So, for deployment it is necessary to host your own gitlab runner, otherwise gitlab cd pipeline will fail.

### Important (security) steps after deployment
- change admin password (starting credentials: username:admin password: admin)
- change user password or delete user (starting credentials: username: user password: user)
- set up all models that should be included 
    - sign up as admin 
    - navigate to entities -> models
    - add models from currently supported vendors along with your API keys 
        - modelname needs to be correct and an active model from the given vendor
        - provider needs to be one fo the supported vendors
            - openai
            - google
            - mistral
            - anthropic
- set up backups for db