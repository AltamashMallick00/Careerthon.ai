package com.careerthon.service;

import org.springframework.stereotype.Service;

@Service
public class LlmService {

    public String generateResponse(String toolName, String userInput) {
        switch (toolName) {
            case "Headline Generator":
                return "1. Senior Full Stack Engineer | React & Spring Boot Expert | Architecting Scalable SaaS Solutions\n" +
                       "2. Engineering Lead @ InnovateTech | Building 2026's most disruptive Fintech platforms\n" +
                       "3. Specialized AI/ML Developer | Python, TensorFlow, AWS | Transforming data into strategic intelligence\n" +
                       "4. Technical Product Manager | Bridging the gap between engineering excellence and business value\n" +
                       "5. Cloud Architect (Azure/AWS) | Helping enterprise teams scale to 10M+ active users";
            
            case "Bio Rewriter":
                return "**BOLD STYLE:**\nPassionate architect of digital solutions with 8+ years of experience driving multi-million dollar efficiencies. I don't just write code; I build legacies.\n\n" +
                       "**STORY STYLE:**\nMy journey started with a single line of C++ and evolved into leading global engineering teams. Today, I focus on the intersection of human-centric design and high-performance backend systems.\n\n" +
                       "**FORMAL STYLE:**\nDedicated technical professional with a proven track record in full-stack development, cross-functional leadership, and strategic project delivery.";
            
            case "Achievement Generator":
                return "• Engineered a distributed microservices architecture that reduced latency by 45% for 1.2M monthly users.\n" +
                       "• Spearheaded the migration of legacy data to AWS RDS, resulting in a $50k annual reduction in infrastructure costs.\n" +
                       "• Mentored 12 junior developers, increasing team velocity by 30% through structured code reviews and workshops.";

            case "Thank You Email Gen":
                return "Subject: Thank You - [Role Name] Interview\n\nDear [Interviewer Name],\n\nThank you for the opportunity to discuss the [Role] position today. I was particularly impressed by [Company]'s approach to [Specific Topic discussed].\n\nI am confident that my experience in [Skill] makes me a strong fit for your team. I look forward to the possibility of working together.\n\nBest regards,\n[Your Name]";

            default:
                return "The Careerthon.AI AI is processing your request for '" + toolName + "'.\n\n" +
                       "Strategic Analysis: Your profile shows high potential for growth in this dimension. We recommend focusing on quantifying your impact and aligning your skills with 2026 market demands.\n\n" +
                       "Next Steps: Detailed roadmap for '" + toolName + "' is being generated based on your latest activity.";
        }
    }
}
